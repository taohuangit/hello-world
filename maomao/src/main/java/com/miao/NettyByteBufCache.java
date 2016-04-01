package com.miao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.miao.util.LogUtil;

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
	
	private static Logger logger = LogUtil.getCommonLog();

	public static final ByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
	
	private static final long CLEAN_PERIOD = 3;
	
	private static Map<Integer, QueueCache> queueCaches = new HashMap<Integer, NettyByteBufCache.QueueCache>();

	private static class AllocThread implements Runnable {
		public void run() {
			queueCaches.put(4096, new QueueCache(1024, 4096));
			queueCaches.put(8192, new QueueCache(1024, 8192));
			queueCaches.put(256, new QueueCache(1024*1024, 256));
			for (QueueCache cache : queueCaches.values()) {
				for (int i = 0; i < cache.size; i++) {
					ByteBuf buffer = allocator.buffer(cache.length);
					// 确保是本线程释放
					buffer.retain();
					cache.queue.offer(buffer);
				}
			}
			
			while (!Thread.currentThread().isInterrupted()) {
				
				for (QueueCache cache : queueCaches.values()) {
					if (cache.cleanQueue.size() < 100) {
						continue;
					}
					final List<ByteBuf> toClean = new ArrayList<ByteBuf>(cache.cleanQueue.size());
					cache.cleanQueue.drainTo(toClean);
					for (ByteBuf buf : toClean) {
						ReferenceCountUtil.release(buf);
					}
					for (int i = 0; i < toClean.size(); i++) {
						ByteBuf buffer = allocator.buffer(cache.length);
						// 确保是本线程释放
						buffer.retain();
						cache.queue.offer(buffer);
					}
				}
				try {
					TimeUnit.SECONDS.sleep(CLEAN_PERIOD);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void init() {
		Thread thread = new Thread(new AllocThread(), "NettyByteBufCache-allocator");
		thread.setDaemon(true);
		thread.start();
	}

	public static void flushData(ChannelHandlerContext ctx, byte[] data, int maxSize) {
		if (data == null || data.length > maxSize) {
			return;
		}
		final ByteBuf buf;
		final QueueCache queueCache = queueCaches.get(maxSize);
		
		if (queueCache == null) {
			return;
		}
		buf = queueCache.queue.poll();
		if (buf == null) {
			logger.info("not buff :" + maxSize);
			return;
		}
		
		try {
			buf.writeBytes(data);
			Channel channel = ctx.channel();
			if (channel.isWritable()) {
				ChannelPromise promise = channel.newPromise();
				promise.addListener(new GenericFutureListener<Future<Void>>() {

					public void operationComplete(Future<Void> future) throws Exception {
						queueCache.cleanQueue.put(buf);
					}
					
				});
				ctx.writeAndFlush(buf, promise);
			} else {
				logger.info("no writeable: ");
				queueCache.cleanQueue.put(buf);
			}
		} catch (Exception e) {
			logger.info(e);
		}
	}

	
	static class QueueCache {
		private int length;
		
		private int size;
		
		private ConcurrentLinkedQueue<ByteBuf> queue;
		
		private ArrayBlockingQueue<ByteBuf> cleanQueue;
		
		public QueueCache(int size, int length) {
			this.size = size;
			this.length = length;
			queue = new ConcurrentLinkedQueue<ByteBuf>();
			cleanQueue = new ArrayBlockingQueue<ByteBuf>(4096);
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public ConcurrentLinkedQueue<ByteBuf> getQueue() {
			return queue;
		}

		public void setQueue(ConcurrentLinkedQueue<ByteBuf> queue) {
			this.queue = queue;
		}

		public ArrayBlockingQueue<ByteBuf> getCleanQueue() {
			return cleanQueue;
		}

		public void setCleanQueue(ArrayBlockingQueue<ByteBuf> cleanQueue) {
			this.cleanQueue = cleanQueue;
		}

		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}
		
	}
}