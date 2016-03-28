package com.miao;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class Room {
	private Integer roomId;
	
	public Room(Integer id) {
		this.roomId = id;
	}
	
	private ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
	
	public void sendBarrage(Barrage barrage) {
		for (Connection conn : connections.values()) {
			final ByteBuf buf = ByteBufCache.alloc();
			if (buf == null) {
				return;
			}
			String s = roomId + ":" + barrage.getBarrage();
			buf.writeBytes(s.getBytes());
			ChannelHandlerContext ctx = conn.getCtx();
			Channel channel = ctx.channel();
			if (channel.isWritable()) {
				ChannelPromise promise = channel.newPromise();
				promise.addListener(new GenericFutureListener<Future<Void>>() {

					public void operationComplete(Future<Void> future) throws Exception {
						ByteBufCache.release(buf);
					}
					
				});
				channel.writeAndFlush(buf, promise);
			}
		}
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}
	
	public void remove(String connId) {
		connections.remove(connId);
	}
	
	public void addIfAbsent(Connection conn) {
		connections.putIfAbsent(conn.getId(), conn);
	}

	public ConcurrentHashMap<String, Connection> getConnections() {
		return connections;
	}

	public void setConnections(ConcurrentHashMap<String, Connection> connections) {
		this.connections = connections;
	}
	
}
