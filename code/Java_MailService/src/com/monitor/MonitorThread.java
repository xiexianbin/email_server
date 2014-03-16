package com.monitor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import com.util.Util;

public class MonitorThread implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			ServerSocket server = new ServerSocket(8888, 0, InetAddress.getLocalHost());
			Util.saveLog("成功启动：配置服务器ip："+InetAddress.getLocalHost().getHostAddress()+"端口号："+server.getLocalPort()+"，时间"+Util.getNowTime());
			//时刻监听该端口，当有用户接入时，返回用户配置菜单。
			while(true){
				Socket clientSocket = server.accept();
				//当客户段接入时，启动一个线程，实现配置服务器信息。
				MonitorSubThread msth = new MonitorSubThread(clientSocket);
				Thread clinetThread = new Thread(msth);
				clinetThread.start();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
