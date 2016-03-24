package com.miao;

import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
	private static ConcurrentHashMap<Short, Room> rooms;
	
	static {
		rooms = new ConcurrentHashMap<Short, Room>();
		rooms.put((short) 1, new Room());
	}
	
	public static Room getRoom(Short rid) {
		return rooms.get(rid);
	}
}
