package com.miao;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

public class ChannelManager {
	private ConcurrentHashMap<String, Channel> clients = new ConcurrentHashMap<String, Channel>();
}
