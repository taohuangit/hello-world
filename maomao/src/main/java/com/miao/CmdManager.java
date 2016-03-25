package com.miao;

import java.io.UnsupportedEncodingException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.miao.util.ByteUtil;

import io.netty.channel.ChannelHandlerContext;

public class CmdManager {
	public static void cmd(ChannelHandlerContext ctx, byte[] src) throws UnsupportedEncodingException {
		int cmd = ByteUtil.getShort(src, 4);
		String id = ctx.channel().id().toString();
		JSONObject json = JSON.parseObject(new String(src, 6, src.length-6, "utf-8"));
		switch (cmd) {
		case COMMAND.LOGIN:
			Connection conn = new Connection();
			conn.setId(id);
			conn.setCurrentRoomId((short)0);
			conn.setPreviousRoomId((short)0);
			CommunicationProvider.addIfAbsent(conn);
			break;
		case COMMAND.LOGOUT:
			break;
		case COMMAND.INTO_ROOM:
			
			CommunicationProvider.changeRoom(id, json.getShort("rid"));
			break;
		case COMMAND.SEND_MSG:
			String msg = json.getString("msg");
			String dst = json.getString("dst");
			if (dst == null) {
				CommunicationProvider.sendRoomMsg(id, msg);
			} else {
				CommunicationProvider.sendMsg(id, dst, msg);
			}
			break;
		default:
			break;
		}
	}
}
