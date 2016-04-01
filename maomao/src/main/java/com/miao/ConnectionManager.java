package com.miao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSON;
import com.miao.Connection.RoomStatus;
import com.miao.Connection.UserStatus;
import com.miao.client.general.GeneralBarrage;
import com.miao.util.ByteUtil;

import io.netty.channel.ChannelHandlerContext;

public class ConnectionManager {
	
	private static ConcurrentHashMap<Integer, Room> rooms = new ConcurrentHashMap<Integer, Room>();
	
	private static LinkedBlockingQueue<Barrage> barrages = new LinkedBlockingQueue<Barrage>();
	
	private static ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
	
	private static Thread barrageSendThread;
	
	private static volatile boolean shutdown;
	
	static {
		for (int i = 1; i <= 20; i++) {
			rooms.put(i, new Room(i));
		}
		
		barrageSendThread = new BarrageSendThread();
		barrageSendThread.start();
	}
	
	public static void intoRoom(Connection conn) {
		if (conn == null || conn.getRoomStatus() != Connection.RoomStatus.OUTROOM) {
			return;
		}
		Map<Integer, RoomInfo> infos = conn.getRooms();
		if (infos == null || infos.isEmpty()) {
			return;
		}
		for (RoomInfo e : infos.values()) {
			Room room = rooms.get(e.getRoomId());
			if (room == null) {
				continue;
			}
			if (e.getStatus() == 1) {
				room.addIfAbsent(conn);
			}
		}
		Connection preConn = connections.putIfAbsent(conn.getConnId(), conn);
		if (preConn == null) {
			conn.setRoomStatus(Connection.RoomStatus.INROOM);
		}
	}
	
	public static void outofRoom(String connId) {
		Connection conn = connections.get(connId);
		if (conn == null || conn.getRoomStatus() != Connection.RoomStatus.INROOM) {
			return;
		}
		Connection preConn = connections.remove(conn.getConnId());
		if (preConn == null) {
			return;
		}
		Map<Integer, RoomInfo> infos = conn.getRooms();
		if (infos == null || infos.isEmpty()) {
			return;
		}
		for (RoomInfo e : infos.values()) {
			Room room = rooms.get(e.getRoomId());
			if (room == null) {
				continue;
			}
			room.remove(conn.getConnId());
		}
		conn.setRoomStatus(RoomStatus.OUTROOM);
	}
	
	public static void sendBarrage(Connection conn, Barrage barrage) {
		if (conn.getRoomStatus() != RoomStatus.INROOM || conn.getUserStatus() != UserStatus.ON) {
			return;
		}
		Integer roomId = barrage.getRoomId();
		Room room = rooms.get(roomId);
		if (room == null) {
			return;
		}
		if (!conn.getRooms().containsKey(roomId)) {
			return;
		}
		
		try {
			barrages.put(barrage);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	public static void roomList(ChannelHandlerContext ctx) {

	}
	
	public static void sendMsg(String connId, String toConnId, String msg) {
		
	}
	
	public static Room getRoom(Integer roomId) {
		return rooms.get(roomId);
	}
	
	public static ConcurrentHashMap<Integer, Room> getRooms() {
		return rooms;
	}
	
	static class BarrageSendThread extends Thread {
		
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted() && !shutdown) {
				try {
					Barrage barrage = barrages.take();
					
					GeneralBarrage generalBarrage = new GeneralBarrage(barrage);
					
					byte[] json = JSON.toJSONString(generalBarrage).getBytes();
					byte[] data = new byte[6 + json.length];
					ByteUtil.putInt(data, 0, json.length);
					ByteUtil.putShort(data, 4, COMMAND.SEND_BARRAGE_RESPONSE);
					System.arraycopy(json, 0, data, 6, json.length);
					if (data.length > 256) {
						return;
					}
					Room room = getRoom(barrage.getRoomId());
					if (room == null) {
						return;
					}
					for (Connection c : room.getConnections().values()) {
						ChannelHandlerContext ctx = c.getCtx();
						if (ctx == null || !ctx.channel().isActive()) {
							room.remove(c.getConnId());
							continue;
						}
						if (!ctx.channel().isWritable()) {
							continue;
						}
						NettyByteBufCache.flushData(ctx, data, 256);
					}
				} catch (InterruptedException e) {
					
				}
			}
		}
	}
}
