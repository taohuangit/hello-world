package com.miao.client.general;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.COMMAND;
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

public class TestGeneralClient {
	
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
                	ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(4096, 0, 4, 2, 0));
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
    	
    	private final AtomicInteger count = new AtomicInteger();
    	
    	final Map<Integer, Set<String>> users = new HashMap<Integer, Set<String>>();
    	
    	@Override
    	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
    		// heartbeat
    		SendHeartBeat heartBeat = new SendHeartBeat();
    		heartBeat.setHb("keepalive");
    		byte[] heartBeatJson = JSON.toJSONString(heartBeat).getBytes();
    		byte[] heartbeatData = new byte[6 + heartBeatJson.length];
    		ByteUtil.putInt(heartbeatData, 0, heartBeatJson.length);
    		ByteUtil.putShort(heartbeatData, 4, (short)COMMAND.HEARTBEAT_REQUEST);
    		System.arraycopy(heartBeatJson, 0, heartbeatData, 6, heartBeatJson.length);
    		ctx.writeAndFlush(ctx.alloc().buffer(heartbeatData.length).writeBytes(heartbeatData));
    		
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
    		ByteUtil.putShort(loginData, 4, (short)COMMAND.LOGIN_REQUEST);
    		System.arraycopy(loginJson, 0, loginData, 6, loginJson.length);
    		ctx.writeAndFlush(ctx.alloc().buffer(loginData.length).writeBytes(loginData));
    		
    		// into room
    		IntoRoomRequest intoRoomRequest = new IntoRoomRequest();
    		intoRoomRequest.setRid(String.valueOf(3));
    		byte[] intoRoomJson = JSON.toJSONString(intoRoomRequest).getBytes();
    		byte[] intoRoomData = new byte[6 + intoRoomJson.length];
    		ByteUtil.putInt(intoRoomData, 0, intoRoomJson.length);
    		ByteUtil.putShort(intoRoomData, 4, (short)COMMAND.INTO_ROOMS);
    		System.arraycopy(intoRoomJson, 0, intoRoomData, 6, intoRoomJson.length);
    		ctx.writeAndFlush(ctx.alloc().buffer(intoRoomData.length).writeBytes(intoRoomData));

    		// send barrage
    		final int roomId = Integer.valueOf(intoRoomRequest.getRid());
    		
    		ctx.executor().scheduleAtFixedRate(new Runnable() {
				public void run() {

					if (count.incrementAndGet() > 10) {
						
//						ctx.close();
					}
					
					SendBarrageRequest barrage = new SendBarrageRequest();
					barrage.setRid(roomId);
					barrage.setCnt(RandomStringUtils.randomAscii(10));
					
					byte[] barrageJson = JSON.toJSONString(barrage).getBytes();
					byte[] barrageData = new byte[6 + barrageJson.length];
					ByteUtil.putInt(barrageData, 0, barrageJson.length);
					ByteUtil.putShort(barrageData, 4, (short) COMMAND.SEND_BARRAGE_REQUEST);
					System.arraycopy(barrageJson, 0, barrageData, 6, barrageJson.length);
					ctx.writeAndFlush(ctx.alloc().buffer(barrageData.length).writeBytes(barrageData));
				}
				
			}, 1000, new Random().nextInt(100)+1, TimeUnit.MILLISECONDS);
    		
    	}
    	
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
			ByteBuf buf = (ByteBuf) msg;
			byte[] dst = new byte[buf.readableBytes()];
			buf.readBytes(dst);
//			System.out.println(BinHexUtil.binToHex(dst));
			CharsetDecoder decoder = Charset.forName("utf-8").newDecoder();
			
			JSONObject json = JSON.parseObject(dst, 6, dst.length - 6, decoder, JSONObject.class);
			
			short cmd = ByteUtil.getShort(dst, 4);
			switch (cmd) {
			case COMMAND.SEND_BARRAGE_RESPONSE:
				int rid = json.getIntValue("rid");
				String username = json.getJSONObject("issuer").getString("uname");
				Set<String> names = users.get(rid);
				if (names == null) {
					names = new HashSet<String>();
					users.put(rid, names);
				}
				names.add(username);
				break;

			default:
				break;
			}
			
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
