package com.miao.client.admin;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.COMMAND;
import com.miao.client.general.IntoRoomRequest;
import com.miao.client.general.LoginRequest;
import com.miao.client.general.SendBarrageRequest;
import com.miao.client.general.SendHeartBeat;
import com.miao.util.BinHexUtil;
import com.miao.util.ByteUtil;
import com.miao.util.LogUtil;

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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class TestAdminClient {
	
	private static Logger logger = LogUtil.getClientLog();
	
    public static void main(String[] args) throws Exception {
    	int count = 1;
    	for (int i = 0; i < count; i++) {
    		new Thread(new Runnable() {
				
				public void run() {
					try {
						doRun();
					} catch (Exception e) {
						logger.info(e);
					}
				}
			}).start();
    	}
    }
    
    public static void doRun() throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                	ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(8192, 0, 4, 2, 0));
                    ch.pipeline().addLast(new InHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect("127.0.0.1", 8084).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }    	
    }
    
    static class InHandler extends ChannelInboundHandlerAdapter {
    	
    	@Override
    	public void channelActive(final ChannelHandlerContext ctx) throws Exception {

    		// login
    		LoginRequest loginRequest = new LoginRequest();
    		loginRequest.setPlatform("blz");
    		loginRequest.setUid(new Random().nextInt(99) + 1);
    		loginRequest.setUname(ctx.channel().localAddress().toString());
    		loginRequest.setTs(System.currentTimeMillis());
    		loginRequest.setSign("sign" + loginRequest.getUid());
    		byte[] loginJson = JSON.toJSONString(loginRequest).getBytes();
    		byte[] loginData = new byte[6 + loginJson.length];
    		ByteUtil.putInt(loginData, 0, loginJson.length);
    		ByteUtil.putShort(loginData, 4, COMMAND.SERVER_STATUS);
    		System.arraycopy(loginJson, 0, loginData, 6, loginJson.length);
    		ctx.writeAndFlush(ctx.alloc().buffer(loginData.length).writeBytes(loginData));

    	}
    	
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
			ByteBuf buf = (ByteBuf) msg;
			byte[] dst = new byte[buf.readableBytes()];
			buf.readBytes(dst);
//			System.out.println(BinHexUtil.binToHex(dst));
			CharsetDecoder decoder = Charset.forName("utf-8").newDecoder();
			
			JSONObject json = JSON.parseObject(dst, 6, dst.length - 6, decoder, JSONObject.class);
			
			System.out.print(BinHexUtil.binToHex(dst, 0, 6));
			System.out.println(json);
			
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
