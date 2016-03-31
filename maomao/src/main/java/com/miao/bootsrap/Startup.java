package com.miao.bootsrap;

import com.miao.NettyByteBufCache;
import com.miao.client.admin.AdminClient;
import com.miao.client.general.GeneralClient;

public class Startup {
	public static void main(String[] args) {
		
		NettyByteBufCache.init();
		
		new Thread(new Runnable() {
			
			public void run() {
				new AdminClient(8084).init();
			}
		}).start();
		
		new Thread(new Runnable() {
			
			public void run() {
				GeneralClient client = new GeneralClient(8080);
				client.init();
			}
		}).start();
		

	}
}
