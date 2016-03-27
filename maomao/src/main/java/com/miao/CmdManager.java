package com.miao;

import java.io.UnsupportedEncodingException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.util.ByteUtil;

import io.netty.channel.ChannelHandlerContext;

public class CmdManager {
	public static void cmd(ChannelHandlerContext ctx, byte[] src) throws UnsupportedEncodingException {
		short cmd = ByteUtil.getShort(src, 4);
		String connId = ctx.channel().id().toString();
		JSONObject json = JSON.parseObject(new String(src, 6, src.length-6, "utf-8"));
		
		switch (cmd) {
		
		case COMMAND.INTO_ROOM:
			
			CommunicationProvider.intoRoom(connId, json.getShort("rid"));
			break;
			
		case COMMAND.OUTOF_ROOM:
			
			break;
		case COMMAND.SEND_BARRAGE:
			
			break;
			
		case COMMAND.SEND_MSG:
			break;
		default:
			break;
		}
	}
}
