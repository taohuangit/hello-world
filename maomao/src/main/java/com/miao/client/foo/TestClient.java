package com.miao.client.foo;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.NettyByteBufCache;
import com.miao.util.BinHexUtil;
import com.miao.util.ByteUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TestClient {
	
    public static void main(String[] args) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new InHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect("127.0.0.1", 8080).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
    
    static class InHandler extends ChannelInboundHandlerAdapter {
    	
    	private FooSendHeartbeatTask heartbeatTask;
    	
    	@Override
    	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
    		FooSendHeartbeat heartbeat = new FooSendHeartbeat();
    		byte[] json = JSON.toJSONString(heartbeat).getBytes();
    		ByteBuf buf = ctx.alloc().buffer(24 + json.length);
    		byte[] header = new byte[24];
    		ByteUtil.putInt(header, 0, json.length);
    		buf.writeBytes(header, 0, header.length);
    		buf.writeBytes(json, 0, json.length);
    		
    		// test
    		byte[] bb = new byte[header.length + json.length];
    		System.arraycopy(header, 0, bb, 0, header.length);
    		System.arraycopy(json, 0, bb, header.length, json.length);
    		
    		System.out.println(BinHexUtil.binToHex(bb));
    		
    		ctx.writeAndFlush(buf);
    		
    		super.channelActive(ctx);
    		
    		heartbeatTask = new FooSendHeartbeatTask(ctx);
    		
    		final int roomId = new Random().nextInt(10);
    		final String name = ctx.channel().id().toString();
    		
    		ctx.executor().scheduleAtFixedRate(new Runnable() {
				
				public void run() {
					FooReceiveBarrage barrage = new FooReceiveBarrage();
					barrage.setContent(RandomStringUtils.randomAscii(10));
					barrage.setFromname(name);
					barrage.setRoomid(roomId);
					
					byte[] json = JSON.toJSONString(barrage).getBytes();
					byte[] header = new byte[ClientProfile.HEADER_LENGTH];
					ByteUtil.putInt(header, 0, json.length);
					
		    		byte[] bb = new byte[header.length + json.length];
		    		System.arraycopy(header, 0, bb, 0, header.length);
		    		System.arraycopy(json, 0, bb, header.length, json.length);
		    		
		    		NettyByteBufCache.flushData(ctx, bb);
				}
				
			}, 10, 2, TimeUnit.SECONDS);
    	}
    	
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
			ByteBuf buf = (ByteBuf) msg;
			byte[] dst = new byte[buf.readableBytes()];
			buf.readBytes(dst);
			
//			System.out.println(BinHexUtil.binToHex(dst));
			
			CharsetDecoder decoder = Charset.forName("utf-8").newDecoder();
			
			JSONObject json = JSON.parseObject(dst, ClientProfile.HEADER_LENGTH, dst.length - ClientProfile.HEADER_LENGTH, decoder, JSONObject.class);
			
			String cmdid = json.getString("cmdid");
			if (cmdid.equals(ClientProfile.CMD_KEEPALIVE)) {
				heartbeatTask.setLastTime(System.currentTimeMillis());
				NettyByteBufCache.flushData(ctx, dst);
			}
			
			
			System.out.println(json);
		
			ctx.fireChannelRead(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
