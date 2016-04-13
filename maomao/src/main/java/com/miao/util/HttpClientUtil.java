package com.miao.util;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class HttpClientUtil {
	
	static HttpClient httpClient;
	
	public static HttpClient getClient() {
		return httpClient;
	}
	
	public static String get(String url, Map<String, String> params) {
		HttpClient client = HttpClientUtil.getClient();

		HttpGet request = new HttpGet(getUrl(url, params));
		
		try {
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	static {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(5);
		cm.setDefaultMaxPerRoute(20);
//			HttpHost localHost = new HttpHost("http://ot.n", 80);
//			cm.setMaxPerRoute(new HttpRoute(localHost), 50);
		CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).build();
		httpClient = client;
		
		/*
		String[] uris = new String[] {
				"http://dc.ot.netease.com"
				};
		
		GetThread[] threads = new GetThread[uris.length];
		for (int i = 0; i < threads.length; i++) {
			HttpGet httpGet = new HttpGet(uris[i]);
			threads[i] = new GetThread(client, httpGet);
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
	}
	
	static class GetThread extends Thread {
		final CloseableHttpClient httpClient;
		
		final HttpContext httpContext;
		
		final HttpGet httpGet;
		
		public GetThread(CloseableHttpClient httpClient, HttpGet httpGet) {
			this.httpClient = httpClient;
			this.httpContext = HttpClientContext.create();
			this.httpGet = httpGet;
		}
		
		@Override
		public void run() {
			try {
				CloseableHttpResponse response = httpClient.execute(httpGet, httpContext);
				try {
					HttpEntity entity = response.getEntity();
				} finally {
					response.close();
				}
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	static class IdleConnectionMonitorThread extends Thread {
		final HttpClientConnectionManager cm;
		
		volatile boolean shutdown;
		
		public IdleConnectionMonitorThread(HttpClientConnectionManager cm) {
			this.cm = cm;
		}
		
		@Override
		public void run() {
			while (!shutdown) {
				synchronized (this) {
					try {
						wait(5000);
						cm.closeExpiredConnections();
						cm.closeIdleConnections(30, TimeUnit.MINUTES);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		public void shutdown() {
			shutdown = true;
			synchronized (this) {
				notifyAll();
			}
		}
	}
	
	public static String getUrl(String url, Map<String, String> params) {
		StringBuilder queryStr = new StringBuilder(url);
		if (params != null && !params.isEmpty()) {
			queryStr.append("?");
			for (Map.Entry<String, String> param : params.entrySet()) {
				queryStr.append(param.getKey());
				queryStr.append("=");
				queryStr.append(param.getValue() != null ? param.getValue() : "");
				queryStr.append("&");
			}
			queryStr.deleteCharAt(queryStr.length() - 1);
		}
		return queryStr.toString();
	}
	
}
