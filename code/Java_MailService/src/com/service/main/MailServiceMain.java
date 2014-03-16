package com.service.main;


import java.io.IOException;

import com.bean.LocalServiceBean;
import com.initializa.Initilize;
import com.monitor.MonitorThread;
import com.onceClass.OnceClass;
import com.receiveMail.ReceiveMailService;
import com.receiveMail.ReceiveMailThread;
import com.sendMail.SendMailService;
import com.sendMail.SendMailThread;
import com.util.Util;


public class MailServiceMain {
	/**
	 * 	邮件服务器的主函数，启动服务器进程。
	 *  邮件服务器线程包括：
	 *  	1.接收邮件服务器。POP3
	 *  	2.发送邮件服务器。SMTP
	 * 	服务器配置信息：
	 * 		1.接收邮件服务器端口
	 * 		2.发送邮件服务器端口
	 */
	//游离块，初始化信息
	static {
		Initilize init = new Initilize();
		init.initUser();
	}
	//标识两个服务器的运行状态
	public static boolean stmpFlag = false;
	public static boolean pop3Flag = false;
	public static void main(String[] args) {
		boolean flag = false; //标识服务器的运行状态

		//获取线程的运行状态
		LocalServiceBean localService = OnceClass.getOnce().getLocalServiceInfo();
		if(localService.getServiceState().equals("running")){
			//初始化线程启动状态
			flag = true;
		}else if(localService.getServiceState().equals("shutdown")){
			flag = false;
		}
		//分别实例化receiveMmailThread和sendMailThread对象，实例化线程对象，启动mail服务器。
		if(flag){
			//启动接收邮件服务器进程
			ReceiveMailService rms = new ReceiveMailService();
			Thread rmsTh = new Thread(rms);
			rmsTh.start();
			//启动发送邮件服务器进程
			SendMailService sms = new SendMailService();
			Thread smsTh = new Thread(sms);
			smsTh.start();
		}else{
			Util.saveLog("主线程启动成功，但SMTP服务器和POP３服务器被关闭。请管理员登录启动。时间:"+Util.getNowTime());
		}
		//启动监控进程，时刻监控用户的信息，包括设置配置信息。
		MonitorThread moni = new MonitorThread();
		Thread moniTh = new Thread(moni);
		moniTh.start();
		
		System.out.println("邮件服务器主进程启动成功。时间:"+Util.getNowTime());
		//死循环扫描程序
		OnceClass once = OnceClass.getOnce();
//		while(true){
//			//sleep();缓解服务器压力
//			if(once.getServerSocketMap().size() != 0){
//				if(stmpFlag){
//					try {
//						once.getServerSocketMap().get("sendMailServerSocket").close();
//						//启动发送邮件服务器进程
//						SendMailService sms = new SendMailService();
//						Thread smsTh = new Thread(sms);
//						smsTh.start();
//						
//						stmpFlag = false;
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				if(pop3Flag){
//					try {
//						once.getServerSocketMap().get("receiveMailServerSocket").close();
//						//启动接收邮件服务器进程
//						ReceiveMailService rms = new ReceiveMailService();
//						Thread rmsTh = new Thread(rms);
//						rmsTh.start();
//						
//						pop3Flag = false;
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}
	}
}
