package com.miao.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.miao.util.ByteUtil;

public class TestClient {
	
	public static void cmd(short cmd, String value) throws UnknownHostException, IOException {
		Socket socket = new Socket("127.0.0.1", 8080);
		
		byte[] header = new byte[6];
		byte[] json = value.getBytes();
		
		ByteUtil.putInt(header, 0, json.length);
		ByteUtil.putShort(header, 4, cmd);
		
		OutputStream os = socket.getOutputStream();
		os.write(header);
		os.write(json);
		
		InputStream is = socket.getInputStream();
		byte[] dst = new byte[1024];
		while (is.read(dst) != -1) {
			System.out.println(new String(dst));
		}
		
		socket.close();
	}
}
