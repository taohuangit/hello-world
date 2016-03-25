package com.miao;

public class Connection {
	private String id;
	
	private Short currentRoomId;
	
	private Short previousRoomId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Short getCurrentRoomId() {
		return currentRoomId;
	}

	public void setCurrentRoomId(Short currentRoomId) {
		this.currentRoomId = currentRoomId;
	}

	public Short getPreviousRoomId() {
		return previousRoomId;
	}

	public void setPreviousRoomId(Short previousRoomId) {
		this.previousRoomId = previousRoomId;
	}
	
	
}
