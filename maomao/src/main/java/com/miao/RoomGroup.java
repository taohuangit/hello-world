package com.miao;

import java.util.concurrent.ConcurrentHashMap;

public class RoomGroup {
	private Short gid;
	
	private ConcurrentHashMap<Integer, User> users;
}
