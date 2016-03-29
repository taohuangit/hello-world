package com.miao;

public class Barrage {
	private String connId;
	
	private User user;
	
	private Integer roomId;
	
	private String barrage;

	public String getConnId() {
		return connId;
	}

	public void setConnId(String connId) {
		this.connId = connId;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
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
