package com.miao.client.admin;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.COMMAND;
import com.miao.Connection;
import com.miao.ConnectionManager;
import com.miao.NettyByteBufCache;
import com.miao.Room;
import com.miao.util.ByteUtil;
import com.miao.util.LogUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class AdminClient {
	private static Logger logger = LogUtil.getCommonLog();
	
	private int port;
	
	public AdminClient(int p) {
		this.port = p;
	}
	
	public void init() {
		EventLoopGroup group = new NioEventLoopGroup(1);

		try {
			ServerBootstrap server = new ServerBootstrap();
			server.group(group);
			server.channel(NioServerSocketChannel.class);
			server.option(ChannelOption.SO_BACKLOG, 1024);
			server.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel sc) throws Exception {
					sc.pipeline().addLast(new LengthFieldBasedFrameDecoder(4096, 0, 4, 2, 0));
					sc.pipeline().addLast(new InHandler());
				}
			});
			
			try {
				ChannelFuture future = server.bind(port).sync();
				
				future.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				group.shutdownGracefully();
			}
			
		} finally {
			
		}
	}
	
	static class InHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			ByteBuf buf = (ByteBuf) msg;
			byte[] dst = new byte[buf.readableBytes()];
			buf.readBytes(dst);
			
			short cmd = ByteUtil.getShort(dst, 4);
			
			CharsetDecoder decoder = Charset.forName("utf-8").newDecoder();
			JSONObject json = null;
			try {
				json = JSON.parseObject(dst, 6, dst.length-6, decoder, JSONObject.class);
			} catch (Exception e) {
				logger.info(e);
			}
			if (json == null) {
				ctx.close();
				return;
			}
			
			switch (cmd) {
			case COMMAND.SERVER_STATUS:
				
				ConcurrentHashMap<Integer, Room> rooms = ConnectionManager.getRooms();

				Map<Integer, TreeSet<String>> roomConns = new HashMap<Integer, TreeSet<String>>();
				for (Room room : rooms.values()) {
					TreeSet<String> set = new TreeSet<String>();
					for (Connection c : room.getConnections().values()) {
						set.add(c.getConnId());
					}
					roomConns.put(room.getRoomId(), set);
				}
				
				byte[] roomInfo = JSON.toJSONString(roomConns).getBytes();
				
				byte[] data = new byte[6 + roomInfo.length];
				ByteUtil.putInt(data, 0, roomInfo.length);
				ByteUtil.putShort(data, 4, COMMAND.SERVER_STATUS);
				System.arraycopy(roomInfo, 0, data, 6, roomInfo.length);
				
				System.out.println(json);
				
				NettyByteBufCache.flushData(ctx, data, 8192);
				
				break;

			default:
				ctx.fireChannelRead(msg);
				break;
			}
		}
	}
}
