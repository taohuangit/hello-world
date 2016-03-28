package com.miao;

public class Barrage {
	private String connId;
	
	private User user;
	
	private short roomId;
	
	private String barrage;

	public String getConnId() {
		return connId;
	}

	public void setConnId(String connId) {
		this.connId = connId;
	}

	public short getRoomId() {
		return roomId;
	}

	public void setRoomId(short roomId) {
		this.roomId = roomId;
	}

	public String getBarrage() {
		return barrage;
	}

	public void setBarrage(String barrage) {
		this.barrage = barrage;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
