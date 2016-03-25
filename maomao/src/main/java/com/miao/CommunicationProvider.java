package com.miao;

import java.util.concurrent.ConcurrentHashMap;

public class CommunicationProvider {
	private static ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
	
	private static ConcurrentHashMap<Short, Room> rooms = new ConcurrentHashMap<Short, Room>();
	
	public static Connection addIfAbsent(Connection conn) {
		return connections.putIfAbsent(conn.getId(), conn);
	}
	
	public static void changeRoom(String connectionId, Short roomId) {
		Connection conn = connections.get(connectionId);
		Room previousRoom = rooms.get(conn.getPreviousRoomId());
		previousRoom.remove(connectionId);
		
		Room currentRoom = rooms.get(conn.getCurrentRoomId());
		currentRoom.addIfAbsent(conn);
	}
	
	public static void sendRoomMsg(String connectionId, String msg) {
		Room room = rooms.get(connections.get(connectionId).getCurrentRoomId());
		room.sendRoomMsg(connectionId, msg);
	}
	
	public static void sendMsg(String connId, String toConnId, String msg) {
		Connection toConn = connections.get(toConnId);
		Room room = rooms.get(toConn.getCurrentRoomId());
		room.sendMsg(connId, toConnId, msg);
	}
}
