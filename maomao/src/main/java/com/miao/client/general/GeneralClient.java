package com.miao.client.general;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.Barrage;
import com.miao.COMMAND;
import com.miao.Connection;
import com.miao.Connection.RoomStatus;
import com.miao.Connection.UserStatus;
import com.miao.ConnectionManager;
import com.miao.Room;
import com.miao.RoomInfo;
import com.miao.User;
import com.miao.util.ByteUtil;
import com.miao.util.LogUtil;

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

public class GeneralClient {
	
	private static Logger logger = LogUtil.getCommonLog();
	
	private int port;
	
	private static final AtomicInteger count = new AtomicInteger();
	
	public GeneralClient(int p) {
		this.port = p;
	}
	
	public void init() {
		EventLoopGroup bossgroup = new NioEventLoopGroup(4);
		EventLoopGroup workergroup = new NioEventLoopGroup(4);
		System.out.println(bossgroup);
		System.out.println(workergroup);
		
		logger.info("startup");

		try {
			ServerBootstrap server = new ServerBootstrap();
			server.group(bossgroup, workergroup);
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
				bossgroup.shutdownGracefully();
				workergroup.shutdownGracefully();
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
			ConnectionManager.outofRoom(ctx.channel().id().toString());
			logger.info("out close:" + ctx + " count: " + count.decrementAndGet());
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
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ConnectionManager.outofRoom(ctx.channel().id().toString());
			logger.info("out exceptionCaught:" + ctx + " count: " + count.decrementAndGet());
		}
	}
	
	static class InHandler extends ChannelInboundHandlerAdapter {
		
		private Connection conn;
		
		/**
		 * see AbstractChannel.AbstractUnsafe
		 * Only fire a channelActive if the channel has never been registered. This prevents firing
		 * multiple channel actives if the channel is deregistered and re-registered.
		 * @param ctx
		 * @param msg
		 * @throws Exception
		 */
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			conn = new Connection();
			conn.setConnId(ctx.channel().id().toString());
			conn.setCtx(ctx);
			conn.setRooms(null);
			conn.setUser(null);
			conn.setRoomStatus(RoomStatus.OUTROOM);
			conn.setUserStatus(UserStatus.OFF);
			
			count.incrementAndGet();
		}
		
		/**
		 * length|header|body
		 */
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
				// TODO: handle exception
			}
			if (json == null) {
				conn.setMsgErrorCount(conn.getMsgErrorCount() + 1);
				ctx.close();
				return;
			}
			
			switch (cmd) {
			case COMMAND.INTO_ROOMS:
				if (conn.getRoomStatus() == RoomStatus.INROOM) {
					ConnectionManager.outofRoom(conn.getConnId());
					conn.setRoomStatus(RoomStatus.OUTROOM);
				}
				String rid = json.getString("rid");
				if (rid.contains("+")) {
					conn.setRooms(new HashMap<Integer, RoomInfo>());
					for (String s : rid.split("+")) {
						Integer roomId = Integer.valueOf(s);
						Room room = ConnectionManager.getRoom(roomId);
						if (room == null) {
							return;
						}
						conn.getRooms().put(roomId, new RoomInfo(roomId, 1));
					}
				} else {
					Integer roomId = Integer.valueOf(rid);
					Room room = ConnectionManager.getRoom(roomId);
					if (room == null) {
						return;
					}
					conn.setRooms(new HashMap<Integer, RoomInfo>());
					conn.getRooms().put(roomId, new RoomInfo(roomId, 1));
				}
				
				ConnectionManager.intoRoom(conn);
				break;
			case COMMAND.OUTOF_ROOMS:
				ConnectionManager.outofRoom(conn.getConnId());
				break;
			case COMMAND.SEND_BARRAGE_REQUEST:
				if (conn.getRoomStatus() != RoomStatus.INROOM || conn.getUserStatus() != UserStatus.ON) {
					return;
				}

				int roomId = json.getIntValue("rid");
				if (!conn.getRooms().containsKey(roomId)) {
					return;
				}
				String content = json.getString("cnt");
				Barrage barrage = new Barrage();
				barrage.setConnId(conn.getConnId());
				barrage.setUser(conn.getUser());
				barrage.setRoomId(roomId);
				barrage.setContent(content);
				barrage.setIp(conn.getCtx().channel().remoteAddress().toString());
				
				ConnectionManager.sendBarrage(conn, barrage);
				break;

			case COMMAND.LOGIN_REQUEST:
				if (conn.getUserStatus() == UserStatus.ON) {
					return;
				}
				String platform = json.getString("platform");
				int uid = json.getIntValue("uid");
				String username = json.getString("uname");
				long timestamp = json.getLongValue("ts");
				String sign = json.getString("sign");
				
				User user = new User();
				user.setPlatform(platform);
				user.setUid(uid);
				user.setUsername(username);
				conn.setUser(user);
				conn.setUserStatus(UserStatus.ON);
				break;
			default:
				
//				conn.setMsgErrorCount(conn.getMsgErrorCount());
				break;
			}
		}
		
		/**
		 * see AbstractChannel.AbstractUnsafe
		 * We are now registered to the EventLoop. It's time to call the callbacks for the ChannelHandlers,
         * that were added before the registration was done.
		 * @param ctx
		 * @throws Exception
		 */
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
			close(conn);
			logger.info("in channelInactive:" + ctx + " count: " + count.decrementAndGet());
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
			logger.info(cause);
			close(conn);
			logger.info("in exceptionCaught:" + ctx);
		}
		
		private void close(Connection conn) {
			if (conn.getRoomStatus() == RoomStatus.INROOM) {
				ConnectionManager.outofRoom(conn.getConnId());
			}
			conn.getCtx().close();
		}
		
	}

	
	public static void main(String[] args) {
		new GeneralClient(8080).init();
	}
}
