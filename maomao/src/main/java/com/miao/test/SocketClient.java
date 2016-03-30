package com.miao.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.miao.COMMAND;
import com.miao.util.ByteUtil;

public class SocketClient {

	private static ConcurrentHashMap<Integer, Set<Integer>> roomUser = new ConcurrentHashMap<Integer, Set<Integer>>();

	public static void main(String[] args) throws IOException, InterruptedException {
		int count = 1000;
		CountDownLatch latch = new CountDownLatch(count);
		for (int i = 0; i < 100; i++) {
			new DataThread(latch).start();
		}
		
		TimeUnit.SECONDS.sleep(20);
		System.out.println(roomUser);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("q", "query");
		TestClient.cmd(COMMAND.ROOM_INFO, JSON.toJSONString(params));
		
		
	}

	private static class DataThread extends Thread {
		
		private CountDownLatch latch;
		
		
		public DataThread(CountDownLatch barrier) {
			this.latch = latch;
		}
		
		@Override
		public void run() {
			Socket socket;
			try {
				socket = new Socket("127.0.0.1", 8080);

				System.out.println(socket);

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("rid", new Random().nextInt(5));
				map.put("uid", Integer.valueOf(socket.getLocalPort()));
				String json = JSON.toJSONString(map);

				byte[] header = new byte[6];
				byte[] bjson = json.getBytes();

				ByteUtil.putInt(header, 0, bjson.length);
				ByteUtil.putShort(header, 4, COMMAND.INTO_ROOMS);

				OutputStream os = socket.getOutputStream();
				// for (byte b : header) {
				// TimeUnit.MILLISECONDS.sleep(1);
				os.write(header);
				// }
				//
				// for (byte b : bjson) {
				// TimeUnit.MILLISECONDS.sleep(1);
				os.write(bjson);
				// }

				Map<String, Object> barrage = new HashMap<String, Object>();
				barrage.put("barrage", socket.getLocalPort());
				String bJson = JSON.toJSONString(barrage);
				byte[] bHeader = new byte[6];
				byte[] barrageJson = bJson.getBytes();
				ByteUtil.putInt(bHeader, 0, barrageJson.length);
				ByteUtil.putShort(bHeader, 4, COMMAND.SEND_BARRAGE);

				os.write(bHeader);
				os.write(barrageJson);

				InputStream is = socket.getInputStream();
				byte[] dst = new byte[1024];
				int n = 0;
				while ((n = is.read(dst)) != -1) {
					String v = new String(dst, 0, n);
					System.out.println(socket.getLocalPort() + ": " + v);
					synchronized (SocketClient.class) {
						String[] args = v.split(",");
						for (String s : args) {
							String[] ss = s.split(":");
							if (ss.length == 2) {
								Set<Integer> users = roomUser.get(Integer.valueOf(ss[0]));
								if (users == null) {
									users = new HashSet<Integer>();
								}
								users.add(Integer.valueOf(ss[1]));
								roomUser.putIfAbsent(Integer.valueOf(ss[0]), users);
							}
						}
					}
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
