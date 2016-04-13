package com.miao.bootsrap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.miao.Room;
import com.miao.util.HttpClientUtil;

public class RoomCache {
	private static Map<Integer, Room> rooms;
	
	public static void init() {
		
	}
	
	public static void update() {
		String source = HttpClientUtil.get("http://114.113.197.230:8080/imaccount/livetv/rooms", null);
		
		JSONObject json = JSON.parseObject(source);
		JSONArray array = json.getJSONArray("rooms");
		if (array == null || array.size() == 0) {
			return;
		}
		
		Map<Integer, Room> temp = new HashMap<Integer, Room>();
		for (int i = 0; i < array.size(); i++) {
			JSONObject j = array.getJSONObject(i);
			Room room = new Room(j.getInteger("roomId"));
			room.setPlatform(j.getString("platform"));
			room.setPlatformRoomId(j.getString("platform_room_id"));
			
			temp.put(room.getRoomId(), room);
			
		}
		
		synchronized (RoomCache.class) {
			rooms = Collections.unmodifiableMap(temp);
		}
		
	}
	
	public static Map<Integer, Room> getRooms() {
		return rooms;
	}
}
