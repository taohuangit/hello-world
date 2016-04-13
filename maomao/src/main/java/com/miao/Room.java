package com.miao;

import java.util.concurrent.ConcurrentHashMap;

public class Room {
	private Integer roomId;
	
	private String platform;
	
	private String platformRoomId;
	
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
		connections.putIfAbsent(conn.getConnId(), conn);
	}

	public ConcurrentHashMap<String, Connection> getConnections() {
		return connections;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getPlatformRoomId() {
		return platformRoomId;
	}

	public void setPlatformRoomId(String platformRoomId) {
		this.platformRoomId = platformRoomId;
	}

	public void setConnections(ConcurrentHashMap<String, Connection> connections) {
		this.connections = connections;
	}
	
}
