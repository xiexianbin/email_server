package com.receiveMail;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import com.bean.LocalServiceBean;
import com.onceClass.OnceClass;
import com.util.Util;

public class ReceiveMailService implements Runnable {
	/**
	 * 接收邮件的服务器线程：POP3
	 */
	//某个用户的接收邮件的服务器
	
	//获取服务器的配置信息
	LocalServiceBean localService = OnceClass.getOnce().getLocalServiceInfo();
	
	//接受服务器进程运行标志。
	private boolean receiveServerFlag = true;
	public void setPOP3ServiceState(){
		if(this.receiveServerFlag == false){
			this.receiveServerFlag = true;
		}else{
			this.receiveServerFlag = false;
		}
	}
	@Override
	public void run() {
		//实例化接收邮件服务器的线程，端口号从userBean中获取
		int port = Integer.parseInt(localService.getPop3Port());
		String ip = "0.0.0.0";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ServerSocket receiveMailServerSocket = new ServerSocket(port, 0, InetAddress.getLocalHost());
			Util.saveLog("成功启动：POP3邮件服务器ip："+ip+"端口号："+receiveMailServerSocket.getLocalPort()+"，时间"+Util.getNowTime());
			//把启动信息保存到serverSocketMap中
			OnceClass.getOnce().getServerSocketMap().put("receiveMailServerSocket", receiveMailServerSocket);
			while(receiveServerFlag){
				Socket receiveSocket = receiveMailServerSocket.accept();
				ReceiveMailThread receMail = new ReceiveMailThread(receiveSocket);
				Thread receTh = new Thread(receMail);
				receTh.start();
//				System.out.println(receiveSocket);
			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
//			e1.printStackTrace();
			Util.saveLog("POP3服务器ip："+ip+"端口号："+port+"停止运行。时间："+Util.getNowTime());
		}
	}
}
