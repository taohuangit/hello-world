package com.miao;

import java.util.concurrent.ConcurrentHashMap;

public class CommunicationProvider {
	private static ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
	
	private static ConcurrentHashMap<Short, Room> rooms = new ConcurrentHashMap<Short, Room>();
	
	public static Connection addIfAbsent(Connection conn) {
		return connections.putIfAbsent(conn.getId(), conn);
	}
	
	public static void intoRoom(String connId, Short roomId) {
		Connection conn = connections.get(connId);
		if (conn == null) {
			conn = new Connection();
			conn.setId(connId);
			conn.setCurrentRoomId(roomId);
			conn.setPreviousRoomId((short)-1);
			connections.put(connId, conn);
		} else {
			conn.setPreviousRoomId(conn.getCurrentRoomId());
			conn.setCurrentRoomId(roomId);
			Room previousRoom = rooms.get(conn.getPreviousRoomId());
			previousRoom.remove(connId);
			Room currentRoom = rooms.get(conn.getCurrentRoomId());
			currentRoom.addIfAbsent(conn);
		}
	}
	
	public static void outofRoom(String connId) {
		Connection conn = connections.remove(connId);
		if (conn != null) {
			Room room = rooms.get(conn.getCurrentRoomId());
			room.remove(conn.getId());
		}
	}
	
	public static void sendBarrage(String connId, String msg) {
		Connection conn = connections.get(connId);
		if (conn != null) {
			Room room = rooms.get(conn.getCurrentRoomId());
			room.sendRoomMsg(connId, msg);
		}
	}
	
	public static void sendMsg(String connId, String toConnId, String msg) {
		Connection toConn = connections.get(toConnId);
		Room room = rooms.get(toConn.getCurrentRoomId());
		room.sendBarrage(connId, toConnId, msg);
	}
}
