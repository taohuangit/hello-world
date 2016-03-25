package com.miao;

import java.util.concurrent.ConcurrentHashMap;

public class Room {
	private Short roomId;
	
	private ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
	
	public void sendRoomMsg(String id, String msg) {
		
	}
	
	public void sendMsg(String id, String toId, String msg) {
		
	}

	public Short getRoomId() {
		return roomId;
	}

	public void setRoomId(Short roomId) {
		this.roomId = roomId;
	}
	
	public void remove(String connId) {
		connections.remove(connId);
	}
	
	public void addIfAbsent(Connection conn) {
		connections.putIfAbsent(conn.getId(), conn);
	}
}
