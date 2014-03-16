package com.bean;

import java.io.Serializable;

public class ForeignServiceBean implements Serializable {

	/**
	 * 这里存储的是其他邮件服务器的基本信息//最终把他们添加到foreignServiceBean中
	 * 		邮件提供商名称/如：qq.com/163.com/sina.com
	 * 		服务器的mx记录
	 *		 邮件提供商的SMTP服务器的端口号
	 */
	private String serviceName;
	private String stmpMx;
	private String stmpPort;
	//构造器
	public ForeignServiceBean(String name, String smx, String sport){
		this.serviceName = name;
		this.stmpMx = smx;
		this.stmpPort = sport;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getStmpMx() {
		return stmpMx;
	}
	public void setStmpMx(String stmpMx) {
		this.stmpMx = stmpMx;
	}
	public String getStmpPort() {
		return stmpPort;
	}
	public void setStmpPort(String stmpPort) {
		this.stmpPort = stmpPort;
	}
}
