package com.miao;

import io.netty.channel.ChannelHandlerContext;

public class Connection {
	private String id;
	
	private User user;
	
	private Integer currentRoomId;
	
	private Integer previousRoomId;
	
	private ChannelHandlerContext ctx;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getCurrentRoomId() {
		return currentRoomId;
	}

	public void setCurrentRoomId(Integer currentRoomId) {
		this.currentRoomId = currentRoomId;
	}

	public Integer getPreviousRoomId() {
		return previousRoomId;
	}

	public void setPreviousRoomId(Integer previousRoomId) {
		this.previousRoomId = previousRoomId;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
