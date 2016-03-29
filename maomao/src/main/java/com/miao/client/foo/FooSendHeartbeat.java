package com.miao.client.foo;

import io.netty.channel.ChannelHandlerContext;

public class FooSendHeartbeat implements Runnable {
	
	private static final long INTERVAL = 90 * 1000;
	
	private long lastTime;
	
	private ChannelHandlerContext ctx;
	
	public void run() {
		if (System.currentTimeMillis() - lastTime > INTERVAL) {
			ctx.close();
		} else {
			ctx.writeAndFlush(null);
		}
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

}
