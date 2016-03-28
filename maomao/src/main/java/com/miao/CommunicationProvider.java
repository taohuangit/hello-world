package com.miao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class CommunicationProvider {
	private static ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
	
	private static ConcurrentHashMap<Short, Room> rooms = new ConcurrentHashMap<Short, Room>();
	
	static {
		rooms.put((short)1, new Room(1));
		rooms.put((short)2, new Room(2));
		rooms.put((short)3, new Room(3));
		rooms.put((short)4, new Room(4));
		rooms.put((short)5, new Room(5));
		rooms.put((short)6, new Room(6));
		rooms.put((short)7, new Room(7));
	}
	
	public static Connection getConnection(String connId) {
		return connections.get(connId);
	}
	
	public static void intoRoom(ChannelHandlerContext ctx, User user, Short roomId) {
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
			conn.setPreviousRoomId((short)-1);
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
			flush(conn.getCtx(), s);
		}
	}
	
	public static void roomInfo(ChannelHandlerContext ctx) {
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
		
		flush(ctx, roomInfo.toString());
	}
	
	public static void sendMsg(String connId, String toConnId, String msg) {
		
	}
	
	private static void flush(ChannelHandlerContext ctx, String msg) {
		final ByteBuf buf = ByteBufCache.alloc();
		if (buf == null) {
			return;
		}
		
		buf.writeBytes(msg.getBytes());
		Channel channel = ctx.channel();
		if (channel.isWritable()) {
			ChannelPromise promise = channel.newPromise();
			promise.addListener(new GenericFutureListener<Future<Void>>() {

				public void operationComplete(Future<Void> future) throws Exception {
					ByteBufCache.release(buf);
				}
				
			});
			channel.writeAndFlush(buf, promise);
		}
	}
}
