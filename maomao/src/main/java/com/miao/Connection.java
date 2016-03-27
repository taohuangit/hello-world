package com.miao;

import io.netty.channel.ChannelHandlerContext;

public class Connection {
	private String id;
	
	private Short currentRoomId;
	
	private Short previousRoomId;
	
	private ChannelHandlerContext ctx;

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

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
