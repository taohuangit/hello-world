package com.miao.client.foo;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.Barrage;
import com.miao.Connection;
import com.miao.Connection.RoomStatus;
import com.miao.Connection.UserStatus;
import com.miao.ConnectionManager;
import com.miao.Room;
import com.miao.RoomInfo;
import com.miao.User;
import com.miao.util.BinHexUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class FooClient {
	
	private int port;
	
	public FooClient(int p) {
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
					sc.pipeline().addLast(new LengthFieldBasedFrameDecoder(4096, 0, 4, 20, 0));
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
	
	static class OutHandler extends ChannelOutboundHandlerAdapter {
		
		
		@Override
		public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			// TODO Auto-generated method stub
			super.disconnect(ctx, promise);
		}
		
		@Override
		public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			// TODO Auto-generated method stub
			super.deregister(ctx, promise);
		}
		
		@Override
		public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
				throws Exception {
			// TODO Auto-generated method stub
			super.bind(ctx, localAddress, promise);
		}
		
		@Override
		public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
				ChannelPromise promise) throws Exception {
			// TODO Auto-generated method stub
			super.connect(ctx, remoteAddress, localAddress, promise);
		}
		@Override
		public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
			// TODO Auto-generated method stub
			super.close(ctx, promise);
		}
		
		@Override
		public void read(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.read(ctx);
		}
		
		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			// TODO Auto-generated method stub
			super.write(ctx, msg, promise);
		}
		
		@Override
		public void flush(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.flush(ctx);
		}
	}
	
	static class InHandler extends ChannelInboundHandlerAdapter {
		
		private FooSendHeartbeatTask heartbeatTask;
		
		private Connection connection;
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			heartbeatTask = new FooSendHeartbeatTask(ctx);
			ctx.executor().execute(heartbeatTask);
			
			connection = new Connection();
			connection.setConnId(ctx.channel().id().toString());
			connection.setCtx(ctx);
			connection.setUser(null);
			Map<Integer, RoomInfo> rooms = new HashMap<Integer, RoomInfo>();
//			rooms.put(1, new RoomInfo(1, 0));
//			rooms.put(2, new RoomInfo(2, 0));
//			rooms.put(3, new RoomInfo(3, 0));
			connection.setRooms(rooms);
			
			// send login
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			// TODO Auto-generated method stub
			ByteBuf buf = (ByteBuf) msg;
			byte[] dst = new byte[buf.readableBytes()];
			buf.readBytes(dst);
			
			CharsetDecoder decoder = Charset.forName("utf-8").newDecoder();
			
			JSONObject json = JSON.parseObject(dst, ClientProfile.HEADER_LENGTH, dst.length - ClientProfile.HEADER_LENGTH, decoder, JSONObject.class);
			
			System.out.print(BinHexUtil.binToHex(dst, 0, ClientProfile.HEADER_LENGTH));
			System.out.println(json);
			
			String cmdid = json.getString("cmdid");
			if (cmdid.equals(ClientProfile.CMD_KEEPALIVE)) {
				heartbeatTask.setLastTime(System.currentTimeMillis());
			} else if (cmdid.equals(ClientProfile.CMD_THIRDCHATMSG)) {
				if (connection.getRoomStatus() != RoomStatus.INROOM || connection.getUserStatus() != UserStatus.ON) {
					return;
				}
				int roomId = json.getIntValue("rid");
				String content = json.getString("cnt");
				String name = json.getString("name");
				int vod = json.getIntValue("vod");
				Barrage barrage = new Barrage();
				barrage.setConnId(connection.getConnId());
				barrage.setRoomId(roomId);
				barrage.setUser(new User());
				barrage.setContent(content);
				
				ConnectionManager.sendBarrage(connection, barrage);
			} else if (cmdid.equals(ClientProfile.CMD_LOGIN_RESPONSE)) {
				for (RoomInfo r : connection.getRooms().values()) {
					r.setStatus(1);
				}
				ConnectionManager.intoRoom(connection);
			} else if (cmdid.equals(ClientProfile.CMD_INTO_ROOM)) {
				int roomId = json.getIntValue("rid");
				Room room = ConnectionManager.getRoom(roomId);
				if (room == null) {
					
				} else {
					connection.getRooms().put(roomId, new RoomInfo(roomId, 1));
					ConnectionManager.intoRoom(connection);
				}
			} else if (cmdid.equals(ClientProfile.CMD_LOGIN_REQUEST)) {
				String username = json.getString("uname");
				String password = json.getString("pwd");
				User user = new User();
				user.setUid(1);
				user.setUsername(username);
				connection.setUser(user);
				connection.setUserStatus(UserStatus.ON);
			}
		
		}
		
		@Override
		public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelRegistered(ctx);
		}
		
		@Override
		public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelUnregistered(ctx);
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			ConnectionManager.outofRoom(connection.getConnId());
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelReadComplete(ctx);
		}
		
		@Override
		public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
			// TODO Auto-generated method stub
			super.channelWritabilityChanged(ctx);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			
			ConnectionManager.outofRoom(connection.getConnId());
			
			ctx.close();
		}
		
	}
	
	public static void main(String[] args) {
		new FooClient(8080).init();
	}
}
