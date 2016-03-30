package com.miao;

public class RoomInfo {
	private Integer roomId;
	
	private Integer status;

	public RoomInfo(Integer roomId, Integer status) {
		super();
		this.roomId = roomId;
		this.status = status;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
