package com.miao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.miao.Connection.RoomStatus;
import com.miao.Connection.UserStatus;

import io.netty.channel.ChannelHandlerContext;

public class ConnectionManager {
	
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
	
	public static void intoRoom(Connection conn) {
		if (conn == null || conn.getRoomStatus() != Connection.RoomStatus.AUTH) {
			return;
		}
		Map<Integer, RoomInfo> infos = conn.getRooms();
		if (infos == null || infos.isEmpty()) {
			return;
		}
		for (RoomInfo e : infos.values()) {
			Room room = rooms.get(e.getRoomId());
			if (room == null) {
				continue;
			}
			if (e.getStatus() == 1) {
				room.addIfAbsent(conn);
			}
		}
		conn.setRoomStatus(Connection.RoomStatus.INROOM);
	}
	
	public static void outofRoom(Connection conn) {
		if (conn == null || conn.getRoomStatus() != Connection.RoomStatus.INROOM) {
			return;
		}
		Map<Integer, RoomInfo> infos = conn.getRooms();
		if (infos == null || infos.isEmpty()) {
			return;
		}
		for (RoomInfo e : infos.values()) {
			Room room = rooms.get(e.getRoomId());
			if (room == null) {
				continue;
			}
			room.remove(conn.getConnId());
		}
		conn.setRoomStatus(RoomStatus.OUTROOM);
	}
	
	public static void sendBarrage(Connection conn, Barrage barrage) {
		if (conn.getRoomStatus() != RoomStatus.INROOM || conn.getUserStatus() != UserStatus.ON) {
			return;
		}
		Integer roomId = barrage.getRoomId();
		Room room = rooms.get(roomId);
		if (room == null) {
			return;
		}
		if (!conn.getRooms().containsKey(roomId)) {
			return;
		}
		
		byte[] header = new byte[6];
		byte[] json = JSON.toJSONString(barrage).getBytes();
		byte[] data = new byte[header.length + json.length];
		System.arraycopy(header, 0, data, 0, 6);
		System.arraycopy(json, 0, data, header.length, json.length);
		if (data.length > 256) {
			return;
		}
		for (Connection c : room.getConnections().values()) {
			if (c.getCtx() == null || !c.getCtx().channel().isActive()) {
				room.remove(c.getConnId());
				continue;
			}
			NettyByteBufCache.flushData(c.getCtx(), data, 256);
		}
	}
	
	public static void roomList(ChannelHandlerContext ctx) {

	}
	
	public static void sendMsg(String connId, String toConnId, String msg) {
		
	}
	
	public static Room getRoom(Integer roomId) {
		return rooms.get(roomId);
	}
}
