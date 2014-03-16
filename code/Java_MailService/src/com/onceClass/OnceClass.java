package com.onceClass;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.bean.ForeignServiceBean;
import com.bean.LocalServiceBean;
import com.bean.UserBean;

public class OnceClass {
	
	//存放用户信息Map<v, k> v = userName(邮件地址) k = UserBean
	private Map<String, UserBean> userMap = new HashMap<String, UserBean>();
	//在线用户集合
	private Map<String, Socket> onLineMap = new HashMap<String, Socket>();
	//保存本服务器的基本信息
	private LocalServiceBean localServiceInfo = new LocalServiceBean();
	//保存其他网络邮箱的服务器基本信息
	private Map<String, ForeignServiceBean> foreignServiceMap = new HashMap<String, ForeignServiceBean>();
	//保存启动的服务器信息
	private Map<String, ServerSocket> serverSocketMap = new HashMap<String, ServerSocket>();

	//私有化构造器
	private OnceClass(){}
	//实例化当前类的一个对象
	private static OnceClass once ;
	//获取单子类的一个方法
	public static OnceClass getOnce() {
		if(once == null){
			once = new OnceClass();
		}
		return once;
	}
	public Map<String, Socket> getOnLineMap() {
		return onLineMap;
	}
	public Map<String, UserBean> getUserMap() {
		return userMap;
	}
	public void setUserMap(Map<String, UserBean> userMap) {
		this.userMap = userMap;
	}
	public LocalServiceBean getLocalServiceInfo() {
		return localServiceInfo;
	}
	public void setLocalServiceInfo(LocalServiceBean localServiceInfo) {
		this.localServiceInfo = localServiceInfo;
	}
	public Map<String, ForeignServiceBean> getForeignServiceMap() {
		return foreignServiceMap;
	}
	public void setForeignServiceMap(
			Map<String, ForeignServiceBean> foreignServiceMap) {
		this.foreignServiceMap = foreignServiceMap;
	}
	public Map<String, ServerSocket> getServerSocketMap() {
		return serverSocketMap;
	}
}
