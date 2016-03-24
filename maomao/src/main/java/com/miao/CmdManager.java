package com.miao;

import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.util.BinHexUtil;
import com.miao.util.ByteUtil;
import com.miao.util.MD5Util;

import io.netty.channel.ChannelHandlerContext;

public class CmdManager {
	
	
	public static void process(ChannelHandlerContext ctx, byte[] src) {
		int cmd = ByteUtil.getShort(src, 4);
		System.out.println("cmd: " + cmd);
		switch (cmd) {
		case COMMAND.LOGIN:// login
			JSONObject json;
			try {
				json = JSON.parseObject(new String(src, 6, src.length-6));
				Short rid = json.getShort("rid");
				User u = new User();
				u.setUid(0);
				String s = ctx.toString();
				System.out.println(ctx.channel());
				System.out.println(ctx.channel().id());
				System.out.println(s);
				System.out.println(ctx.name());
				String sid = BinHexUtil.binToHex(MD5Util.encrypt(s.getBytes()));
				u.setSid(sid);
				u.setRid(rid);
				Room room = RoomManager.getRoom(rid);
				if (room == null) {
					ctx.close();
					return;
				} 
				ConcurrentHashMap<String, ChannelHandlerContext> connections = room.getConnections();
				if (connections.putIfAbsent(ctx.channel().id().toString(), ctx) != null) {
					return;
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		case 1:
			System.out.println(RoomManager.getRoom((short)1).getConnections().size());
			break;
		default:
			break;
		}
	}
}
