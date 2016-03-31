package com.miao;

import java.io.UnsupportedEncodingException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.util.ByteUtil;

import io.netty.channel.ChannelHandlerContext;

public class CmdManager {
	
	public static void cmd(ChannelHandlerContext ctx, byte[] src) throws UnsupportedEncodingException {
		short cmd = ByteUtil.getShort(src, 4);
		JSONObject json = JSON.parseObject(new String(src, 6, src.length-6, "utf-8"));
		System.out.println(cmd + ": " + json);
		switch (cmd) {
		
		case COMMAND.INTO_ROOMS:
			User user = new User();
			user.setUid(json.getInteger("uid"));
			break;
			
		case COMMAND.OUTOF_ROOMS:
			break;
		case COMMAND.SEND_BARRAGE_REQUEST:
	
			break;
			
		case COMMAND.SEND_MSG:
			break;
			
		case COMMAND.ROOM_LIST:
			ConnectionManager.roomList(ctx);
			break;
		case COMMAND.ROOM_INFO:
			break;
		default:
			break;
		}
	}
}
