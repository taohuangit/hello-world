package com.miao;

import java.util.Map;

import io.netty.channel.ChannelHandlerContext;

public class Connection {
	private ChannelHandlerContext ctx;
	
	private String connId;
	
	private User user;
	
	private Map<Integer, RoomInfo> rooms;
	
	private volatile RoomStatus roomStatus = RoomStatus.INIT;
	
	private volatile UserStatus userStatus = UserStatus.OFF;
	
	private long lastActiveTime;
	
	private int msgErrorCount;
	
	
	
	public enum RoomStatus {
		INIT, REQUEST, AUTH, INROOM, OUTROOM
	}
	
	public enum UserStatus {
		OFF, ON
	}
	
	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public String getConnId() {
		return connId;
	}

	public void setConnId(String connId) {
		this.connId = connId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Map<Integer, RoomInfo> getRooms() {
		return rooms;
	}

	public void setRooms(Map<Integer, RoomInfo> rooms) {
		this.rooms = rooms;
	}

	public RoomStatus getRoomStatus() {
		return roomStatus;
	}

	public void setRoomStatus(RoomStatus status) {
		this.roomStatus = status;
	}

	public UserStatus getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatus userStatus) {
		this.userStatus = userStatus;
	}

	public long getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public int getMsgErrorCount() {
		return msgErrorCount;
	}

	public void setMsgErrorCount(int msgErrorCount) {
		this.msgErrorCount = msgErrorCount;
	}
	
}
