package com.miao.client.foo;

public class FooReceiveBarrage {
	private String cmdid = "thirdchatmsg";
	
	private String content;
	
	private String fromname;
	
	private int roomid;
	
	private int vod = 0;

	public String getCmdid() {
		return cmdid;
	}

	public void setCmdid(String cmdid) {
		this.cmdid = cmdid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFromname() {
		return fromname;
	}

	public void setFromname(String fromname) {
		this.fromname = fromname;
	}

	public int getRoomid() {
		return roomid;
	}

	public void setRoomid(int roomid) {
		this.roomid = roomid;
	}

	public int getVod() {
		return vod;
	}

	public void setVod(int vod) {
		this.vod = vod;
	}
	
}
