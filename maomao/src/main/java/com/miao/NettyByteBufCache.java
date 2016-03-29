package com.miao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;

public class NettyByteBufCache {

	public static final ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;

	private static final BlockingQueue<ByteBuf> bufferQueue = new ArrayBlockingQueue<ByteBuf>(100);

	private static final BlockingQueue<ByteBuf> toCleanQueue = new LinkedBlockingQueue<ByteBuf>();

	private static final int TO_CLEAN_SIZE = 50;

	private static final long CLEAN_PERIOD = 100;
	
	private static final int QUEUE_SIZE = 1024;

	private static class AllocThread implements Runnable {
		public void run() {
			
			for (int i = 0; i < QUEUE_SIZE; i++) {
				try {
					ByteBuf buffer = allocator.buffer();
					// 确保是本线程释放
					buffer.retain();
					bufferQueue.put(buffer);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
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

	public static ByteBuf alloc() {
		try {
			ByteBuf buf = bufferQueue.take();
			System.out.println("alloc: " + buf);
			return buf;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return null;
	}

	public static void release(ByteBuf buf) {
		System.out.println("release: " + buf);
		toCleanQueue.add(buf);
	}

}
