package com.miao.client.foo;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.miao.NettyByteBufCache;
import com.miao.util.ByteUtil;

import io.netty.channel.ChannelHandlerContext;

public class FooSendHeartbeatTask implements Runnable {
	private static byte[] DATA;
	
	private static final long TIMEOUT = 90 * 1000;
	
	private static final int INTERVAL = 30;
	
	private volatile long lastTime;
	
	private ChannelHandlerContext ctx;
	
	public FooSendHeartbeatTask(ChannelHandlerContext ctx) {
		this.ctx = ctx;
		lastTime = System.currentTimeMillis();
	}
	
	static {
		byte[] json = null;
		try {
			json = JSON.toJSONString(new FooSendHeartbeat()).getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DATA = new byte[24 + json.length];
		ByteUtil.putInt(DATA, 0, json.length);
		System.arraycopy(json, 0, DATA, 24, json.length);
	}
	
	public void run() {
		if (!ctx.channel().isActive()) {
			System.out.println("close: " + ctx + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastTime)) );
			return;
		}
		final long now = System.currentTimeMillis();
		if (now - lastTime > TIMEOUT) {
			System.out.println("timeout: " + ctx + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastTime)) );
			ctx.close();
		} else {
			NettyByteBufCache.flushData(ctx, DATA);
			System.out.println("send heartbeat: " + ctx + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastTime)));
			ctx.executor().schedule(this, INTERVAL, TimeUnit.SECONDS);
		}
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		System.out.println("rec heartbeart: " + ctx + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastTime)));
		this.lastTime = lastTime;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
}
