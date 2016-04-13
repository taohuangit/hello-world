package com.miao.client.general;

import com.miao.Barrage;

public class GeneralBarrage {
	
	private Integer rid;
	
	private String cnt;
	
	private Issuer  issuer;
	
	public GeneralBarrage(Barrage barrage) {
		this.rid = barrage.getRoomId();
		this.cnt = barrage.getContent();
		this.issuer = new Issuer();
		issuer.setIp(barrage.getIp());
		issuer.setPlatform(barrage.getUser().getPlatform());
		issuer.setName(barrage.getUser().getUsername());
	}
	
	
	public Integer getRid() {
		return rid;
	}



	public void setRid(Integer rid) {
		this.rid = rid;
	}



	public String getCnt() {
		return cnt;
	}



	public void setCnt(String cnt) {
		this.cnt = cnt;
	}



	public Issuer getIssuer() {
		return issuer;
	}



	public void setIssuer(Issuer issuer) {
		this.issuer = issuer;
	}



	static class Issuer {
		private String platform;
		
		private String name;
		
		private String ip;

		public String getPlatform() {
			return platform;
		}

		public void setPlatform(String platform) {
			this.platform = platform;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}
	}
}
