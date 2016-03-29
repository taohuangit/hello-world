package com.miao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ConnectionManager {
	private static ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
	
	private static ConcurrentHashMap<Integer, Room> rooms = new ConcurrentHashMap<Integer, Room>();
	
	static {
		rooms.put(1, new Room(1));
		rooms.put(2, new Room(2));
		rooms.put(3, new Room(3));
		rooms.put(4, new Room(4));
		rooms.put(5, new Room(5));
		rooms.put(6, new Room(6));
		rooms.put(7, new Room(7));
	}
	
	public static Connection getConnection(String connId) {
		return connections.get(connId);
	}
	
	public static void intoRoom(ChannelHandlerContext ctx, User user, Integer roomId) {
		String connId = ctx.channel().id().toString();
		Connection conn = connections.get(connId);
		Room room = rooms.get(roomId);
		if (room == null) {
			return;
		}
		if (conn == null) {
			conn = new Connection();
			conn.setId(connId);
			conn.setUser(user);
			conn.setCurrentRoomId(roomId);
			conn.setPreviousRoomId(-1);
			conn.setCtx(ctx);
			connections.put(connId, conn);
			room.addIfAbsent(conn);
		} else {
			conn.setPreviousRoomId(conn.getCurrentRoomId());
			conn.setCurrentRoomId(roomId);
			Room previousRoom = rooms.get(conn.getPreviousRoomId());
			previousRoom.remove(connId);
			Room currentRoom = rooms.get(conn.getCurrentRoomId());
			currentRoom.addIfAbsent(conn);
		}
	}
	
	public static void outofRoom(ChannelHandlerContext ctx) {
		String connId = ctx.channel().id().toString();
		Connection conn = connections.remove(connId);
		if (conn != null) {
			Room room = rooms.get(conn.getCurrentRoomId());
			room.remove(conn.getId());
		}
	}
	
	public static void sendBarrage(Barrage barrage) {
		Room room = rooms.get(barrage.getRoomId());
		for (Connection conn : room.getConnections().values()) {
			String s = room.getRoomId() + ":" + barrage.getBarrage() + ",";
			flushData(conn.getCtx(), s);
		}
	}
	
	public static void roomList(ChannelHandlerContext ctx) {
		Map<Integer, List<String>> roomInfo = new HashMap<Integer, List<String>>();
		for (Room room : rooms.values()) {
			List<String> list = new ArrayList<String>();
			if (room.getConnections() == null) {
				continue;
			}
			for (Connection c : room.getConnections().values()) {
				list.add(c.getCtx().channel().remoteAddress() + ":" + c.getUser().getUid());
			}
			roomInfo.put(room.getRoomId(), list);
		}
		
		flushData(ctx, roomInfo.toString());
	}
	
	public static void sendMsg(String connId, String toConnId, String msg) {
		
	}
	
	private static void flushData(ChannelHandlerContext ctx, String data) {
		final ByteBuf buf = NettyByteBufCache.alloc();
		if (buf == null) {
			return;
		}
		
		buf.writeBytes(data.getBytes());
		Channel channel = ctx.channel();
		if (channel.isWritable()) {
			ChannelPromise promise = channel.newPromise();
			promise.addListener(new GenericFutureListener<Future<Void>>() {

				public void operationComplete(Future<Void> future) throws Exception {
					NettyByteBufCache.release(buf);
				}
				
			});
			channel.writeAndFlush(buf, promise);
		}
	}
}
