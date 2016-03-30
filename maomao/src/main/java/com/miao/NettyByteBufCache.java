package com.miao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyByteBufCache {

	public static final ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;

	private static final ConcurrentLinkedQueue<ByteBuf> bufferQueue = new ConcurrentLinkedQueue<ByteBuf>();

	private static final BlockingQueue<ByteBuf> toCleanQueue = new LinkedBlockingQueue<ByteBuf>();

	private static final int TO_CLEAN_SIZE = 50;

	private static final long CLEAN_PERIOD = 100;
	
	private static final int QUEUE_SIZE = 1024;

	private static class AllocThread implements Runnable {
		public void run() {
			
			for (int i = 0; i < QUEUE_SIZE; i++) {
				ByteBuf buffer = allocator.buffer(1024);
				// 确保是本线程释放
				buffer.retain();
				bufferQueue.offer(buffer);
			}
			
			long lastCleanTime = System.currentTimeMillis();
			while (!Thread.currentThread().isInterrupted()) {

				if (toCleanQueue.size() > TO_CLEAN_SIZE || System.currentTimeMillis() - lastCleanTime > CLEAN_PERIOD) {
					final List<ByteBuf> toClean = new ArrayList<ByteBuf>(toCleanQueue.size());
					toCleanQueue.drainTo(toClean);
					for (ByteBuf buffer : toClean) {
						ReferenceCountUtil.release(buffer);
					}
					lastCleanTime = System.currentTimeMillis();
				}
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	static {
		Thread thread = new Thread(new AllocThread(), "qclient-redis-allocator");
		thread.setDaemon(true);
		thread.start();
	}

	public static void flushData(ChannelHandlerContext ctx, byte[] data) {
		flushData(ctx, data, 256);
	}
	
	public static void flushData(ChannelHandlerContext ctx, byte[] data, int maxSize) {
		if (data == null || data.length > maxSize) {
			return;
		}
		final ByteBuf buf;
		final ConcurrentLinkedQueue<ByteBuf> queue;
		switch (maxSize) {
		case 256:
			queue = bufferQueue;
			buf = queue.poll();
			break;
		case 1024:
			queue = null;
			buf = null;
			break;

		case 4096:
			queue = null;
			buf = null;
			break;
		default:
			queue = null;
			buf = null;
			break;
		}
		
		if (buf == null) {
			return;
		} else {
			buf.clear();
		}
		
		buf.writeBytes(data);
		Channel channel = ctx.channel();
		if (channel.isWritable()) {
			ChannelPromise promise = channel.newPromise();
			promise.addListener(new GenericFutureListener<Future<Void>>() {

				public void operationComplete(Future<Void> future) throws Exception {
					queue.offer(buf);
				}
				
			});
			channel.writeAndFlush(buf, promise);
		}
	}

}