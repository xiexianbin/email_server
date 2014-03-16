package com.initializa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.bean.ForeignServiceBean;
import com.bean.LocalServiceBean;
import com.bean.UserBean;
import com.onceClass.OnceClass;
import com.util.Util;

public class Initilize {

	/**
	 * 初始化邮件服务器：
	 * 			初始化userMap
	 * 			初始化本邮件系统的STMP和POP3端口号、邮件的默认存放路径、邮件的后缀（本邮件系统使用的后缀名称）
	 * 			初始化其他邮件服务器的stmp和pop3信息
	 */
	OnceClass once;
	public void initUser(){
		//初始化邮件服务器操作日志文件
		File serviceLogFile = new File(Util.SERVICELOG);
		if(!serviceLogFile.getParentFile().exists()){
			serviceLogFile.getParentFile().mkdirs();
		}
		//初始化userMap
		initUserMap();
		//初始化本邮件系统的STMP和POP3端口号、邮件的默认存放路径、邮件的后缀 localServer
		initLocalHost();
		//初始化其他邮件服务器的stmp和pop3信息
		initForeignService();
	}
	//初始化本邮件系统的STMP和POP3端口号、邮件的默认存放路径、邮件的后缀
	private void initLocalHost() {
		// TODO Auto-generated method stub
		once = OnceClass.getOnce();
		File file = new File(Util.LOCALHOSTINFOPATH);
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		if(file.exists() && file.isFile() && file.length()!=0){
			//若存在本地邮件服务器的基本配置信息，则加载
			BufferedReader br = Util.getBufferedReader(file);
			Properties ps = new Properties();
			try {
				ps.load(br);
				//获取单子类中的local...对象
				LocalServiceBean localService = once.getLocalServiceInfo();
				localService.setMailPath(ps.getProperty("mailPath"));
				localService.setMailSuffix(ps.getProperty("mailSuffix"));
				localService.setPop3Port(ps.getProperty("pop3Port"));
				localService.setServiceState(ps.getProperty("serviceState"));
				localService.setStmpPort(ps.getProperty("stmpPort"));
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			//当不存在本地服务器的基本配置信息时，初始化为默认值
			once = OnceClass.getOnce();
			LocalServiceBean localService = once.getLocalServiceInfo();
			localService.setMailPath(Util.DEFAULTMAILPATH);
			localService.setMailSuffix("@domain.com");
			localService.setPop3Port("110");
			localService.setServiceState("shutdown");
			localService.setStmpPort("25");
			Util.write2LocalServiceFile();
		}
	}
	//初始化userMap
	public void initUserMap(){
		//初始化userMap
		once = OnceClass.getOnce();
		File file = new File(Util.USERMAPPARH);
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		//实例化存放userMap的文件对象
		if(file.exists() && file.length()!=0){
			try {
				ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(file));
				Map<String, UserBean> userMap = (Map<String, UserBean>) objIn.readObject();
				once.setUserMap(userMap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//初始化其他邮件服务器的stmp和pop3信息
	public void initForeignService(){
		File file = new File(Util.FOREIGNSERVICEPATH);
		if(!file.getParentFile().exists()){
			file.getParentFile().exists();
		}
		if(file.exists() && file.isFile() && file.length()!=0){
			once = OnceClass.getOnce();
			try {
				InputStream in = new FileInputStream(file);
				ObjectInputStream objIn = new ObjectInputStream(in);
				Map<String, ForeignServiceBean> foreignServiceMap;
				foreignServiceMap = (Map<String, ForeignServiceBean>) objIn.readObject();
				once.setForeignServiceMap(foreignServiceMap);
				objIn.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
