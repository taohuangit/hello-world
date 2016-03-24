package com.miao.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;

import com.alibaba.fastjson.JSON;
import com.miao.util.BinHexUtil;
import com.miao.util.ByteUtil;

public class SocketClient {
	public static void main(String[] args) throws IOException, InterruptedException {
		for (int i = 0; i < 10; i++) {
			test();
		}
	}
	
	private static void test() throws UnknownHostException, IOException, InterruptedException {
		Socket socket = new Socket("127.0.0.1", 8080);
		System.out.println(socket);
		for (int i = 0; i < 1; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("rid", 1234);
			String json = JSON.toJSONString(map);
			
			byte[] header = new byte[6];
			byte[] bjson = json.getBytes();
			
			ByteUtil.putInt(header, bjson.length, 0);
			ByteUtil.putShort(header, (short)0, 4);
			
			System.out.println(header.length);
			System.out.println(bjson.length);
			System.out.println(BinHexUtil.binToHex(header));
			System.out.println(json);
			System.out.println(BinHexUtil.binToHex(bjson));
			
			OutputStream os = socket.getOutputStream();
//			for (byte b : header) {
//				TimeUnit.MILLISECONDS.sleep(1);
				os.write(header);
//			}
//			
//			for (byte b : bjson) {
//				TimeUnit.MILLISECONDS.sleep(1);
				os.write(bjson);
//			}
			
		}
		socket.close();

	}
}
