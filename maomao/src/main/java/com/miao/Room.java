package com.miao;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;

public class Room {
	private Short rid;
	
	private ConcurrentHashMap<Short, RoomGroup> groups;
	
	private ConcurrentHashMap<String, ChannelHandlerContext> connections = new ConcurrentHashMap<String, ChannelHandlerContext>();

	public ConcurrentHashMap<String, ChannelHandlerContext> getConnections() {
		return connections;
	}
	
	
}
