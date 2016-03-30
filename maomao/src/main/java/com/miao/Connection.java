package com.miao;

import java.util.Set;

import io.netty.channel.ChannelHandlerContext;

public class Connection {
	private ChannelHandlerContext ctx;
	
	private String connId;
	
	private User user;
	
	private Integer roomId;
	
	private Integer preRoomId;
	
	private Set<Integer> extRooms;
	
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

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Set<Integer> getExtRooms() {
		return extRooms;
	}

	public void setExtRooms(Set<Integer> extRooms) {
		this.extRooms = extRooms;
	}

	public Integer getPreRoomId() {
		return preRoomId;
	}

	public void setPreRoomId(Integer preRoomId) {
		this.preRoomId = preRoomId;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
