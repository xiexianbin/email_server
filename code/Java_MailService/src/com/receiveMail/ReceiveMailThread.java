package com.receiveMail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;

import com.base64coder.Base64Coder;
import com.bean.LocalServiceBean;
import com.bean.UserBean;
import com.onceClass.OnceClass;
import com.util.Util;


public class ReceiveMailThread implements Runnable {

	/**
	 * 接收邮件的子线程：
	 * 先询问是否有该用户：
	 * 		1. user a@57901.com // user 123
	 * 		2. pass 123
	 * 		4. stat
	 * 		3. list
	 */
	private Socket client;
	private PrintStream ps;
	private BufferedReader br;
	public ReceiveMailThread(Socket client){
		this.client = client;
		try {
			ps = new PrintStream(this.client.getOutputStream());
			br = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private OnceClass once;
	private Map<String, UserBean> userMap;
	private LocalServiceBean localServer;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		startLinkOk();
		//死循环，判断输入内容。
		while(true){
			try {
				String mess = br.readLine();
				if(mess == null){
					Util.saveLog(Util.getClientInfo(client)+"与POP服务器连接中断。");
					return ;
				}
				//转化为小写，并剔除空格
				mess = mess.toLowerCase().trim();
				System.err.println(mess);
				if(mess.equalsIgnoreCase("noop")){
					//空操作
					doOk();
				}else if(mess.equalsIgnoreCase("quit")){
					//退出
					doOk();
					Util.saveLog(Util.getClientInfo(client)+"正常退出POP服务器。");
					//正常退出
					return ;
				}else if(mess.equalsIgnoreCase("ehlo") || mess.equalsIgnoreCase("helo") || mess.equalsIgnoreCase("ehlo "+Util.getLocalHostName())){
					//
					doEhlo();
				}else if(mess.equalsIgnoreCase("auth login")){
					//用户登录
					ps.println("334 VXNlcm5hbWU6");
					String userNameBASE64 = br.readLine();
					if(userNameBASE64 == null){
						Util.saveLog(Util.getClientInfo(client)+"获取用户名时与POP服务器连接中断。");
						return;
					}
					System.out.println("userNameBASE64"+userNameBASE64);
					//登录成功
					ps.println("334 UGFzc3dvcmQ6");
					String userPassBASE64 = br.readLine().toLowerCase().trim();
					if(userPassBASE64 == null){
						Util.saveLog(Util.getClientInfo(client)+"获取用户密码时与POP服务器连接中断。");
						return ;
					}
					String userName = Base64Coder.deCoder(userNameBASE64);
					String userPass = Base64Coder.deCoder(userPassBASE64);
					once = OnceClass.getOnce();
					userMap = once.getUserMap();
					if(userMap.containsKey(userName) && userMap.get(userName).getUserPass().equals(userPass)){
						//登录成功
						ps.println("235 authentication successfully");
						Util.saveLog(userName+Util.getClientInfo(client)+"登录成功POP服务器。");
						//登录成功后的相关操作//接收邮件的操作。
						if(!receiveMail(userName)){
							Util.saveLog(Util.getClientInfo(client)+"连接异常中断退出POP服务器。");
							return ;
						}else{
							Util.saveLog(Util.getClientInfo(client)+"正常退出POP服务器。");
							return ;
						}
					}else{
						//登录失败
						ps.println("535 authentication failed");
					}
				}else if(mess.startsWith("user ")){
					//获取用户名
					String userName = mess.replace("user ", "");
					String mailName = Util.getMailName(userName);
					//成功
					doOk();
					//获取密码
					String passMess = br.readLine();
					if(passMess == null){
						Util.saveLog(Util.getClientInfo(client)+"在获取密码时连接中断POP服务器。");
						return ;
					}
					//获取密码
					int index = passMess.indexOf(" ");
					String userPass = passMess.substring(index+1);
					
					once = OnceClass.getOnce();
					userMap = once.getUserMap();
					//用户名和密码同时匹配
					if(userMap.containsKey(mailName) && userMap.get(mailName).getUserPass().equals(userPass)){
						//认证成功
						ps.println("+OK 2 messages");
						Util.saveLog(userName+Util.getClientInfo(client)+"成功登录POP服务器。");
						//认证成功后的相关操作
						if(!receiveMail(mailName)){
							Util.saveLog(userName+Util.getClientInfo(client)+"与POP服务器连接异常中断。");
							return ;
						}else{
							//正常退出
							Util.saveLog(userName+Util.getClientInfo(client)+"正常退出POP服务器。");
							return ;
						}
					}else{
						ps.println("-ERR authorization failed");//密码错误，请重新输入。
					}
				}else{
					ps.println("-ERR command not recognized");
//					System.out.println("wrong Command.");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				Util.saveLog(Util.getClientInfo(client)+"与POP服务器连接中断。");
				return ;
			}
		}
	}
	private void doEhlo() {
		// TODO Auto-generated method stub
		ps.println("250-"+Util.getLocalHostIp());
		ps.println("250-PIPELINING");
		ps.println("250-SIZE "+Util.getFileSize());
		ps.println("250-VRFY");
		ps.println("250-ETRN");
		ps.println("250-AUTH LOGIN PLAIN");
		ps.println("250-AUTH=LOGIN PLAIN");
		ps.println("250-ENHANCEDSTATUSCODES");
		ps.println("250-8BITMIME");
		ps.println("250 DSN");
	}
	
	//收取邮件过程
	public boolean receiveMail(String userName){
		//死循环，用户判断登入成功时的相关操作
		while(true){
			once = OnceClass.getOnce();
			localServer = once.getLocalServiceInfo();
			//获取邮件的存放地址
			File file = new File(localServer.getMailPath()+File.separator+userName+File.separator+"ReceiveMail");
			//如果文件夹不存在，则创建
			Util.mkdirs(file);
			File files[] = file.listFiles();
			int count = files.length;
			try {
				//读入发送指令
				String order = br.readLine();
				if(order == null){
					return false;
				}
				System.err.println(order);
				//处理order,           未处理多余空格问题
				order = order.toLowerCase().trim();
				if(order.equals("noop")){
					doOk();
				}else if(order.equals("quit")){
					//退出本线程访问
					doOk();
					return true;
				}else if(order.equals("stat")){
					//返回邮箱状态
					Util.println(ps, "+OK "+files.length+" "+Util.getFileSize(file));
					Util.println(ps, ".");
				}else if(order.equals("list")){
					//返回当前邮箱用户的邮件列表信息，以点结束
					doOk();
					for(int i=0; i<count; i++){
						int size = Util.getFileSize(files[i]);
						Util.println(ps, (i+1)+" "+size);
					}
					ps.println(".");
				}else if(order.contains("top ")){ //top接收一封邮件，并返回该邮件的头
					String[] Int = order.split(" ");
					int num = Integer.parseInt(Int[Int.length-1]);
					if(num > count){
						Util.println(ps, "-ERR - not that many messages only "+count);
						continue;
					}else{
						//返回+OK
						doOk();
						//返回第一封邮件的邮件头
						BufferedReader br = Util.getBufferedReader(files[num]);
						String subMail = null;
						while((subMail=br.readLine())!=null && !subMail.equals("Subject:")){
							Util.println(ps, subMail);
						}
						Util.println(ps, subMail);
						br.close();
						ps.println(".");
					}
				}else if(order.contains("retr ")){
					doOk();
					int num = Integer.parseInt(order.replace("retr ", ""));
					if(num > count){
						Util.println(ps, "-ERR - not that many messages only "+count);
						continue;
					}else{
						//返回+OK
						doOk();
						Util.saveLog(userName+Util.getClientInfo(client)+"收取一封邮件");
						//返回第一封邮件的邮件头
						BufferedReader br = Util.getBufferedReader(files[num-1]);
						String subMail = null;
						while((subMail=br.readLine())!=null){
							Util.println(ps, subMail);
						}
						br.close();
						ps.println(".");
					}
				}else if(order.contains("del ")){
					int num = Integer.parseInt(order.replace("del ", ""));
					if(num > count){
						Util.println(ps, "-ERR not that many messages only "+count);
						continue;
					}else{
						files[num-1].delete();
					}
					doOk();
				}else if(order.equals("uidl")){
					//返回用户文件的uidl，邮件的唯一标识符
					doOk();
					//返回邮件的名字
					for(int i=0; i<files.length; i++){
						Util.println(ps, (i+1)+" "+files[i].getName());
//						System.out.println((i+1)+files[i].getName());
					}
					ps.println(".");
				}else{
					//错误指令
					Util.println(ps, "-ERR - unimplemented");
					continue;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				return false;
			}
		}
	}
	//返回一个+ ok
	private void doOk() {
		// TODO Auto-generated method stub
		ps.println("+OK");
	}
	//当通过telnet连接时，提示信息
	private void startLinkOk() {
		// TODO Auto-generated method stub
		ps.println("+OK "+OnceClass.getOnce().getLocalServiceInfo().getMailSuffix()+" Server POP3 ready");
	}

}
