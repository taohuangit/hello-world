package com.miao;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.util.ByteUtil;

import io.netty.channel.ChannelHandlerContext;

public class CmdManager {
	
	private static LinkedBlockingQueue<Barrage> barrageCache = new LinkedBlockingQueue<Barrage>(1024);
	
	private static Thread barrageSender;
	
	private static volatile boolean shutdown;
	
	static {
		barrageSender = init();
		barrageSender.start();
	}
	
	public static void cmd(ChannelHandlerContext ctx, byte[] src) throws UnsupportedEncodingException {
		short cmd = ByteUtil.getShort(src, 4);
		String connId = ctx.channel().id().toString();
		JSONObject json = JSON.parseObject(new String(src, 6, src.length-6, "utf-8"));
		System.out.println(cmd + ": " + json);
		switch (cmd) {
		
		case COMMAND.INTO_ROOM:
			User user = new User();
			user.setUid(json.getInteger("uid"));
			CommunicationProvider.intoRoom(ctx, user, json.getShort("rid"));
			break;
			
		case COMMAND.OUTOF_ROOM:
			CommunicationProvider.outofRoom(ctx);
			break;
		case COMMAND.SEND_BARRAGE:
			Connection conn = CommunicationProvider.getConnection(connId);
			if (conn == null) {
				break;
			}
			if (conn.getUser() == null) {
				break;
			}
			Barrage barrage = new Barrage();
			barrage.setConnId(connId);
			barrage.setUser(conn.getUser());
			barrage.setRoomId(conn.getCurrentRoomId());
			barrage.setBarrage(json.getString("barrage"));
			try {
				barrageCache.put(barrage);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case COMMAND.SEND_MSG:
			break;
			
		case COMMAND.ROOM_INFO:
			CommunicationProvider.roomInfo(ctx);
			break;
		default:
			break;
		}
	}
	
	private static Thread init() {
		Thread thread = new Thread(new Runnable() {
			
			public void run() {
				while (!Thread.currentThread().isInterrupted() && !shutdown) {
					try {
						Barrage barrage = barrageCache.take();
						CommunicationProvider.sendBarrage(barrage);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		return thread;
	}
}
