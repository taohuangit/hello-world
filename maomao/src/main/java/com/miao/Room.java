package com.miao;

import java.util.concurrent.ConcurrentHashMap;

public class Room {
	private Integer roomId;
	
	private ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
	
	public Room(Integer id) {
		this.roomId = id;
	}
	
	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}
	
	public void remove(String connId) {
		connections.remove(connId);
	}
	
	public void addIfAbsent(Connection conn) {
		connections.putIfAbsent(conn.getId(), conn);
	}

	public ConcurrentHashMap<String, Connection> getConnections() {
		return connections;
	}

	public void setConnections(ConcurrentHashMap<String, Connection> connections) {
		this.connections = connections;
	}
	
}
