package com.miao;

import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSONObject;

public class BarrageQueue {
	private LinkedBlockingQueue<JSONObject> queue = new LinkedBlockingQueue<JSONObject>(1024 * 1024);
	
	public void put(JSONObject json) {
		try {
			queue.put(json);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public JSONObject take() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
}
