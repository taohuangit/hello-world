package com.miao;

public class Barrage {
	private String connId;
	
	private User user;
	
	private Integer platformId;
	
	private Integer roomId;
	
	private String content;

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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getPlatformId() {
		return platformId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setPlatformId(Integer platformId) {
		this.platformId = platformId;
	}
}
