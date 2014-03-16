package com.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import com.bean.LocalServiceBean;
import com.bean.UserBean;
import com.onceClass.OnceClass;

public class Util {

	//工具包
	//存放服务器操作日志的文件位置
	public static final String SERVICELOG = "D://mailBox/service/mailService.log";
	//mail存放的默认路径：
	public static final String DEFAULTMAILPATH = "D://mailBox/userMailPath";
	//存储邮箱用户map的路径：
	public static final String USERMAPPARH = "D://mailBox/userMapPath/userMap.db";
	//存放本地服务器的基本信息的路劲
	public static final String LOCALHOSTINFOPATH = "D://mailBox/service/localServiceInfo.properties";
	//存放其他服务器的基本信息的路径
	public static final String FOREIGNSERVICEPATH = "D://mailBox/service/foreignServiceInfo.db";
	//存放SMTP服务器的运行日志的路径
	public static final String SMTPLOGPATH = "D://mailBox/service/SMTP.log";
	//存放POP服务器的运行日志的路径
	public static final String POP3LOGPATH = "D://mailBox/service/POP3.log";
	
	//管理员帐号和密码
	public static final String ADMIN = "Admin";
	public static final String ADMINPASS = "admin";
	
	//文件的大小
	private static int size;
	//获取已有邮件用户的空间大小
	public static int getFileSize(){
		size = 0;
		File file = new File(OnceClass.getOnce().getLocalServiceInfo().getMailPath());
		if(file.exists())
			digui(file);
		return size;
	}
	public static int getFileSize(File file){
		size = 0;
		if(file.exists() && !file.isFile())
			digui(file);
		return size;
	}
	//递归
	public static void digui(File file){
		File[] files = file.listFiles();
		for(int i=0; i<files.length; i++){
			if(files[i].isFile()){
				size += files[i].length();
			}else{
				digui(files[i]);
			}
		}
	}
	
	//处理含有"<"、">"的字符串
	public static String[] dealString(String str){
		String[] s = str.split(":");
		//处理s[0]
		s[0] = s[0].trim();
		int first = s[0].indexOf(" ");
		int last = s[0].lastIndexOf(" ");
		//如果空格出现的位置并不相同，则做剔除操作
		if(first<last){
			String[] subStr = s[0].split(" ");
			StringBuffer sb = new StringBuffer();
			sb.append(subStr[0]);
			for(int i=1; i<subStr.length; i++){
				if(!subStr[i-1].equals("") && subStr[i].equals("")){
					sb.append(" ");
				}else if(subStr[i-1].equals("") && !subStr[i].equals("")){
					sb.append(subStr[i]);
				}else{
					continue;
				}
			}
			s[0] = sb.toString();
		}
		//处理s[1]
		s[1] = s[1].trim();
		String mailName;
		String size;
		if(!s[1].contains("=")){
			s[1] = s[1].replace("<", "").replace(">", "").trim();
			return s;
		}else{
			int left = s[1].indexOf("<");
			int right = s[1].indexOf(">");
			mailName = s[1].substring(left+1, right).trim();
			int lastCh = str.indexOf("=");
			size = str.substring(lastCh+1).trim();
		}
		String[] ss = {s[0], mailName, size};
		return ss;
	}
	//获取本地的计算机名称
	public static String getLocalHostName(){
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hostName;
	}
	//获取本地的计算机ip
	public static String getLocalHostIp(){
		String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ip;
	}
	//获取邮件账户名称
	public static String getMailName(String mail){
		String mailName = null;
		mail = mail.trim();
		String suffix = OnceClass.getOnce().getLocalServiceInfo().getMailSuffix();
		if(!mail.contains(suffix) ){
			mailName = mail+suffix;
		}else{
			mailName = mail;
		}
		return mailName;
	}
	//获取当前系统时间
	public static String getNowTime(){
		Date date = new Date();
		DateFormat df = DateFormat.getDateTimeInstance();
		return df.format(date);
	}
	//获取客户端输入流对象
	public static PrintStream getPrintStream(Socket clientSocket){
		PrintStream ps = null;
		try {
			ps = new PrintStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ps;
	}
	//获取客户端输出流
	public static BufferedReader getBufferedReader(Socket clientSocket){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return br;
	}
	//获取文件的输出流，以追加方式
	public static BufferedWriter getBufferedWriter(File file){
		BufferedWriter bw = null;
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream out = new FileOutputStream(file, true);
			OutputStreamWriter osw = new OutputStreamWriter(out);
			bw = new BufferedWriter(osw);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bw;
	}
	//获取文件的输出流，以覆盖方式
	public static BufferedWriter getBufferedWriterFg(File file){
		BufferedWriter bw = null;
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream out = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(out);
			bw = new BufferedWriter(osw);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bw;
	}
	//获取文件的输入流
	public static BufferedReader getBufferedReader(File file){
		BufferedReader br = null;
		try {
			FileInputStream in = new FileInputStream(file);
			if(!file.exists()){
				file.createNewFile();
			}
			InputStreamReader isr = new InputStreamReader(in);
			br = new BufferedReader(isr);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return br;
	}
	//定义输入方法
	public static String getInput(BufferedReader br){
		String str = null;
		try {
			str =  br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		return str;
	}
	//定义输出方法
	public static void println(PrintStream ps, String mess){
		ps.println(mess);
		ps.flush();
	}
	public static void print(PrintStream ps, String mess){
		ps.print(mess);
	}
	public static void printlns(PrintStream ps, String mess){
		ps.println(mess);
		ps.print(">>");
	}
	//定义系统保存系统日志的方法
	public static void saveLog(String mess){
		//打印到控制台
		System.out.println(mess);
		//保存到文件//////追加方式
		File serviceLogFile = new File(Util.SERVICELOG);
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(serviceLogFile, true)));
			bw.write(mess+"\r\n");
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//定义系统保存系统日志的方法
	public static void saveSMTPLog(String mess){
		//打印到控制台
		System.out.println(mess);
		//保存到文件//////追加方式
		File serviceLogFile = new File(Util.SMTPLOGPATH);
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(serviceLogFile, true)));
			bw.write(mess+"\r\n");
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//定义系统保存系统日志的方法
	public static void savePop3Log(String mess){
		//打印到控制台
		System.out.println(mess);
		//保存到文件//////追加方式
		File serviceLogFile = new File(Util.POP3LOGPATH);
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(serviceLogFile, true)));
			bw.write(mess+"\r\n");
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//定义获取客户端套接字的方法
	public static String getClientInfo(Socket clientSocket){
		return Util.getNowTime()+"主机ip："+clientSocket.getInetAddress().getHostAddress()+"端口："+clientSocket.getPort();
	}
	//把单子类写入usepMap对应的file中
	public static void write2UserMapFile() {
		File userMapFile = new File(Util.USERMAPPARH);
		Map<String, UserBean> userMap = OnceClass.getOnce().getUserMap();
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(userMapFile));
			//写入，关闭流
			objOut.writeObject(userMap);
			objOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//把foreignServiceMap中的信息保存到文件中
	public static void write2ForeignServiceFile(){
		File file = new File(Util.FOREIGNSERVICEPATH);
		if(!file.getParentFile().exists()){
			file.getParentFile().exists();
		}
		try {
			OutputStream out = new FileOutputStream(file);
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			//获取foreignServiceMap
			objOut.writeObject(OnceClass.getOnce().getForeignServiceMap());
			objOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	//把单子类写入foreignServiceMap对应的file中
	public static void write2LocalServiceFile(){
		File LocalHostFile = new File(Util.LOCALHOSTINFOPATH);
		if(!LocalHostFile.exists()){
			try {
				LocalHostFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Properties ps = new Properties();
		LocalServiceBean lostService = OnceClass.getOnce().getLocalServiceInfo();
		BufferedReader br = Util.getBufferedReader(LocalHostFile);
		BufferedWriter bw = Util.getBufferedWriterFg(LocalHostFile);
		try {
			ps.load(br);
			ps.setProperty("mailPath", lostService.getMailPath());
			ps.setProperty("mailSuffix", lostService.getMailSuffix());
			ps.setProperty("serviceState", lostService.getServiceState());
			ps.setProperty("pop3Port", lostService.getPop3Port());
			ps.setProperty("stmpPort", lostService.getStmpPort());
			ps.store(bw, Util.getNowTime());
			br.close();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//从onLineMap删除当前已登入用户
	public static void deleLogOutUser(String userName) {
		// TODO Auto-generated method stub
		OnceClass.getOnce().getOnLineMap().remove(userName);
	}
	//创建文件夹
	public static void mkdirs(File sendFile) {
		// TODO Auto-generated method stub
		if(!sendFile.exists()){
			sendFile.mkdirs();
		}
	}
	//获取文件名字的方法   20130101121212311.13
	public static String getMailName() {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS"+Math.random());
		Date date = new Date();
		return sdf.format(date);
	}
	//创建文件
	public static void creatNewFile(File sendFile) {
		// TODO Auto-generated method stub
		if(!sendFile.exists()){
			try {
				sendFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
