package com.miao;

import java.util.concurrent.ConcurrentHashMap;

public class Room {
	private Short rid;
	
	private ConcurrentHashMap<Short, RoomGroup> groups;
}
