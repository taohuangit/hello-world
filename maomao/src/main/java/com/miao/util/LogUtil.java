package com.miao.util;

import org.apache.log4j.Logger;

public class LogUtil {
	public static Logger getAccessLog() {
		return Logger.getLogger("access");
	}
	
	public static Logger getLuceneLog() {
		return Logger.getLogger("lucene");
	}
	
	public static Logger getErrorLog() {
		return Logger.getLogger("error");
	}
	
	public static Logger getManageLog() {
		return Logger.getLogger("manage");
	}
	
	public static Logger getContextLog() {
		return Logger.getLogger("context");
	}

	public static Logger getBlzPushLog() {
		return Logger.getLogger("blzpush");
	}
	
	public static Logger getHttpLog() {
		return Logger.getLogger("http");
	}
	
	public static Logger getRedisLog() {
		return Logger.getLogger("redis");
	}
	
	public static Logger getCommonLog() {
		return Logger.getLogger("common");
	}
	
	public static Logger getClientLog() {
		return Logger.getLogger("client");
	}
	
	public static String format(Object ... parmas) {
		StringBuilder sb = new StringBuilder();
		
		for (Object p : parmas) {
			sb.append(p);
			sb.append("$");
		}
		
		return sb.toString();
	}

}
