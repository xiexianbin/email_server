package com.sendMail;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import com.bean.LocalServiceBean;
import com.onceClass.OnceClass;
import com.util.Util;

public class SendMailService implements Runnable {

	/**
	 * 发送邮件的服务器线程：该服务器的端口号从已定义的map中获取
	 */
	
	//SMTP服务器的运行状态
	private boolean flag = true;
	public void setSMTPServiceState(){
		if(this.flag == false){
			this.flag = true;
			System.out.println("设置后的flag:"+this.flag);
		}else{
			this.flag = false;
			System.out.println("设置后的flag:"+this.flag);
		}
		System.out.println("setSMTPServiceState");
	}
	@Override
	public void run() {
		//实例化发送邮件的服务器对象，定义端口号：25
		LocalServiceBean localService = OnceClass.getOnce().getLocalServiceInfo();
		int port = Integer.parseInt(localService.getStmpPort());
		String ip = "0.0.0.0";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			ServerSocket sendMailServerSocket = new ServerSocket(port, 0, InetAddress.getLocalHost());
			Util.saveLog("成功启动：ESMTP邮件服务器ip："+ip+"端口号："+port+"，时间"+Util.getNowTime());
			//把启动信息保存到serverSocketMap中
			OnceClass.getOnce().getServerSocketMap().put("sendMailServerSocket", sendMailServerSocket);
			while(flag){
				//保存启动成功信息
				Socket client = sendMailServerSocket.accept();
				//启动一个发送邮件的线程？？
				SendMailThread smTh = new SendMailThread(client);
				Thread sentTh = new Thread(smTh);
				sentTh.start();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Util.saveLog("SMTP服务器ip："+ip+"端口号："+port+"停止运行。时间："+Util.getNowTime());
		}
	}
}
