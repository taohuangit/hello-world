package com.miao;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;

public class ConnectionManager {
	private static ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
	
	private static ConcurrentHashMap<Integer, Room> rooms = new ConcurrentHashMap<Integer, Room>();
	
	static {
		rooms.put(1, new Room(1));
		rooms.put(2, new Room(2));
		rooms.put(3, new Room(3));
		rooms.put(4, new Room(4));
		rooms.put(5, new Room(5));
		rooms.put(6, new Room(6));
		rooms.put(7, new Room(7));
	}
	
	public static Connection getConnection(String connId) {
		return connections.get(connId);
	}
	
	public static void intoRoom(Connection conn) {

	}
	
	public static void outofRoom(Connection conn) {

	}
	
	public static void sendBarrage(Barrage barrage) {

	}
	
	public static void roomList(ChannelHandlerContext ctx) {

	}
	
	public static void sendMsg(String connId, String toConnId, String msg) {
		
	}
	
}
