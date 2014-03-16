package com.bean;

import com.util.Util;

public class LocalServiceBean {

	//本地服务器的基本配置信息
	private String mailSuffix; //本地所提供的服务邮件后缀 
	private String stmpPort; //本地服务器的stmp端口
	private String pop3Port; //本地服务器的pop3端口
	private String mailPath; //默认的邮件路径
	private String ServiceState; //用户服务的运行状态：running:表示运行正常  shutdown:表示服务关闭。
	public String getMailSuffix() {
		return mailSuffix;
	}
	public void setMailSuffix(String mailSuffix) {
		this.mailSuffix = mailSuffix;
	}
	public String getStmpPort() {
		return stmpPort;
	}
	public void setStmpPort(String stmpPort) {
		this.stmpPort = stmpPort;
	}
	public String getPop3Port() {
		return pop3Port;
	}
	public void setPop3Port(String pop3Port) {
		this.pop3Port = pop3Port;
	}
	public String getMailPath() {
		return mailPath;
	}
	public void setMailPath(String mailPath) {
		this.mailPath = mailPath;
	}
	public String getServiceState() {
		return ServiceState;
	}
	public void setServiceState(String serviceState) {
		ServiceState = serviceState;
	}
}
