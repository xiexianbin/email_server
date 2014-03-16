package com.sendMail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import sun.misc.BASE64Decoder;

import com.base64coder.Base64Coder;
import com.bean.ForeignServiceBean;
import com.bean.LocalServiceBean;
import com.bean.UserBean;
import com.onceClass.OnceClass;
import com.util.Util;


public class SendMailThread implements Runnable {

	/**
	 * 发送邮件的子线程线程：使用SMTP协议
	 */
	private Socket client;
	private PrintStream ps;
	private BufferedReader br;
	
	public SendMailThread(Socket client){
		this.client = client;
		try {
			ps = new PrintStream(client.getOutputStream());
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private OnceClass once;
	private Map<String, UserBean> userMap;
	private LocalServiceBean localService;
	private Map<String, ForeignServiceBean> foreignMap;
	@Override
	public void run() {
		/**
		 * 通过telnet连接时，提供的相关的问答信息。
		 */
		startLinkOk();
		int time = 0; 
		//使用死循环来表示表示会话
		while(true){
			/////////////////////与foxmail断开连接时回报错误：SocketException:Connection reset
			String mess;
			try {
				mess = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
				return ;
			}
			//当读入的为null时，服务器报错，处理异常为：提示
			if(mess == null){
				//与系统连接未知中断///////////因写入日志文件，并结束该进程
				Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
				return ;
			}
			//读取一行字符串，将其转化为小写形式，并剔除开头和结尾的空串
			mess = mess.toLowerCase().trim();
				System.err.println(mess);
			if(mess.equalsIgnoreCase("noop")){
				//空操作，服务器做ok相应
				doOk();
			}else if(mess.startsWith("ehlo") || mess.startsWith("helo")){//以ehlo或helo开头
				//这里是ESMTP协议
				if(mess.equals("ehlo")){
					Util.println(ps, "501 #5.0.0 EHLO requires domain address");
				}else{
					//获取访问者信息
					String connectServer = mess.split(" ")[1];
					once = OnceClass.getOnce();
					localService = once.getLocalServiceInfo();
					//获取userMap
					userMap = once.getUserMap();
					//普通客户端与服务器的通信。
					if(!connectServer.contains(".")){
						Util.println(ps, "250-"+Util.getLocalHostName());
						Util.println(ps, "250-mail");
						Util.println(ps, "250-8BITMIME");
						Util.println(ps, "250-SIZE "+Util.getFileSize());//文件夹的大小
						Util.println(ps, "250-AUTH PLAIN LOGIN");
						Util.println(ps, "250 AUTH=PLAIN LOGIN");
									
						//读取新指令
						String subMess;
						try {
							subMess = br.readLine();
						} catch (IOException e) {
							// TODO Auto-generated catch block
//							e.printStackTrace();
							Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
							return ;
						}
						if(subMess == null){
							//与系统连接未知中断///////////因写入日志文件，并结束该进程
							Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
							return ;
						}
						subMess = subMess.toLowerCase().trim();
    							System.err.println(subMess);
						if(subMess.equals("auth login")){
							//输入用户名
							Util.println(ps, "334 VXNlcm5hbWU6");
							String userNameBase64 = Util.getInput(br);
							if(userNameBase64 == null){
								//与系统连接未知中断///////////因写入日志文件，并结束该进程
								Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
								return ;
							}
							//解密后的用户名
							String userName = Base64Coder.deCoder(userNameBase64);
//								System.err.println(userName);
							//输入密码
							Util.println(ps, "334 UGFzc3dvcmQ6");
							String userPassBase64 = Util.getInput(br);
							if(userPassBase64 == null){
								//与系统连接未知中断///////////因写入日志文件，并结束该进程
								Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
								return ;
							}
							//解密后的用户名
							String userPass = Base64Coder.deCoder(userPassBase64);
//								System.err.println(userPass);
							//判断用户名和密码是否匹配
							if(userMap.containsKey(userName) && userMap.get(userName).getUserPass().equals(userPass)){
								//登录成功
								doLoginOK();
								Util.saveLog(userName+Util.getClientInfo(client)+"登录SMTP服务器成功。");
								doOk();
								//登录成功后，普通用户发送邮件
								sendLocalMail(userName);
							}else{//用户和密码匹配时，操作结束
								//用户名或密码错误
								Util.println(ps, "535 #5.7.0 Authentication failed");
							}
						//auth login同级指令
						}else if(subMess.equals("noop")){
							//空操作
							doOk();
						}else if(subMess.equals("quit")){
							//终止邮件会话，即结束该进程。
							doQuit();
							Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接正常退出。");
							return ;
						}else{
							//错误的指令代码
							doWrongCommand();
						}
					}else{
						//其他服务器与客户端的通信。
						receForeignMail(connectServer);
					}
				}//以ehlo开头的
			}else if(mess.equals("quit")){
				//终止邮件会话，即结束该进程。
				doQuit();
				Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接正常退出。");
				return ;
			}else{
				doWrongCommand();
			}
		}
	}
	//本地邮件服务提供商向本系统发送邮件
	private void sendLocalMail(String userName) {
		//保存发件人
		String sentMailer = "";
		String foreSendMailer = "";
		//保存收件人
		String receMailer = "";
		String foreReceMailer = "";
		// TODO Auto-generated method stub
		while(true){
			//读指令
			String message;
			try {
				message = br.readLine();
			if(message == null){
				//与系统连接未知中断///////////因写入日志文件，并结束该进程
				Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接异常中断。 ");
				return ;
			}
			message = message.toLowerCase().trim();
			//开始mail from
			if(message.equalsIgnoreCase("auth login")){
				//当再次输入auth login时，提示已登陆
				Util.println(ps, "503 #5.5.0 Already authenticated");
			}else if (message.contains("<") && message.contains(">") && message.contains(":")){
				//获取处理完毕的字符串，s[0]：存储的是命令，s[1]存储的是命令
				String s[] = Util.dealString(message);
				//判断邮件是否是一个邮件地址
				String mailName = s[1];
//										if(mailName.endsWith(localService.getMailSuffix()) && userMap.containsKey(mailName)){
				//处理本站内的邮件
				if(s[0].equalsIgnoreCase("mail from")){
					//发件人
					sentMailer = mailName;
					foreSendMailer = message;
					doOk();
				}else if(s[0].equalsIgnoreCase("rcpt to")){
					foreReceMailer = message;
					//收件人
					if(receMailer.equals(""))
						receMailer = mailName;
					else
						receMailer += "/"+mailName;
					doOk();
				}else{
					//处理外站邮件，在本站保留副本后，发送给到目标服务器
					doWrongCommand();
				}
			}else if(message.equalsIgnoreCase("data")){
				//相应data回复
				responseData();
				/////////////////////////////开始发送邮件、、、、、、
				if(receMailer.endsWith(localService.getMailSuffix())){
					//本系统内的邮件发送
					String sendState = sendLocalMail(sentMailer, receMailer);
					if(sendState.equals("NullPointerException")){
						Util.saveLog(userName+Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
						return ;
					}else if(sendState.equals("quit")){
						Util.saveLog(sentMailer+"在"+Util.getNowTime()+"给"+receMailer+"发送了一封邮件成功。");
						return ;
					}else if(sendState.equals("ok")){
						Util.saveLog(sentMailer+"在"+Util.getNowTime()+"给"+receMailer+"发送了一封邮件成功。");
//											return ;
					}
				}else{
					//其他邮件服务提供商的邮件
					String sendState = sendForeignMail(foreSendMailer, foreReceMailer);
					if(sendState.equals("NullPointerException")){
						//空指针异常
						Util.saveLog(userName+Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
						return ;
					}else if(sendState.equals("quit")){
						Util.saveLog(sentMailer+"在"+Util.getNowTime()+"给"+receMailer+"发送了一封邮件成功。");
						return ;
					}else{
						Util.saveLog(sentMailer+"在"+Util.getNowTime()+"给"+receMailer+"发送了一封邮件成功。");
					}
				}
			}else if(message.equals("noop")){
				doOk();
			}else if(message.equals("quit")){
//				doQuit();
				Util.println(ps, "221 Bye");
				Util.saveLog(userName+Util.getClientInfo(client)+"与SMTP服务器连接正常退出。");
				return ;
			}else{
				//错误指令
				doWrongCommand();
//										Util.println(ps, "用户名或密码错误");
			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				Util.saveLog(userName+Util.getClientInfo(client)+"与SMTP服务器连接正常退出。");
				return ;
			}
		}//while(true)
	}
	//其它邮件服务提供商向本系统发送邮件
	public boolean receForeignMail(String conncetServer){
		//反馈服务器连接成功
		Util.println(ps, "250-"+Util.getLocalHostName());
		Util.println(ps, "250-mail");
//		Util.println(ps, "250-SIZE "+Util.getFileSize());//文件夹的大小
		Util.println(ps, "250-8BITMIME");
		System.out.println("250-8BITMIME");
		
		String sentMailer = "";
		String receMailer = "";
		String foreSendMailer = "";
		String foreReceMailer = "";
		int time = 0;
		while(true){
			String mess;
			try {
				mess = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				return false;
			}
//			mess = mess.toLowerCase().trim();
				System.err.println("receForeignMail:"+mess);
			if (mess.contains("<") && mess.contains(">") && mess.contains(":")){
				if(time++==0){
					Util.saveLog(conncetServer+Util.getNowTime()+"与服务器建立连接成功。");
				}
				//获取处理完毕的字符串，s[0]：存储的是命令，s[1]存储的是命令
				String s[] = Util.dealString(mess);
				//判断邮件是否是一个邮件地址
				String mailName = s[1];
//				if(mailName.endsWith(localService.getMailSuffix()) && userMap.containsKey(mailName)){
				//处理本站内的邮件
				if(s[0].equalsIgnoreCase("mail from")){
					//发件人
					sentMailer = mailName;
					foreSendMailer = mess;
					doOk();
				}else if(s[0].equalsIgnoreCase("rcpt to")){
					foreReceMailer = mess;
					//收件人
					if(receMailer.equals(""))
						receMailer = mailName;
					else
						receMailer += "/"+mailName;
					doOk();
				}else{
					//处理外站邮件，在本站保留副本后，发送给到目标服务器
					doWrongCommand();
				}
			}else if(mess.equalsIgnoreCase("data")){
				//相应data回复
				responseData();
				/////////////////////////////开始发送邮件、、、、、、
				if(receMailer.endsWith(localService.getMailSuffix())){
					//本系统内的邮件发送
					String sendState = receForeignMailContext(receMailer);
					if(sendState.equals("NullPointerException")){
						Util.saveLog(sentMailer+Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
						return false;
					}else if(sendState.equals("quit")){
						Util.saveLog(sentMailer+"在"+Util.getNowTime()+"给"+receMailer+"发送了一封邮件成功。");
						return true;
					}else if(sendState.equals("ok")){
						Util.saveLog(sentMailer+"在"+Util.getNowTime()+"给"+receMailer+"发送了一封邮件成功。");
//										return ;
					}
				}else if(mess.equalsIgnoreCase("quit")){
					//终止邮件会话，即结束该进程。
					doQuit();
					Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接正常退出。");
					return true;
				}else{
					doWrongCommand();
				}
			}
		}//while(true)
	}
	//接收外来的邮件
	private String receForeignMailContext(String receMailer) {
		//创建文件
		File file = new File(Util.DEFAULTMAILPATH+File.separator+receMailer+File.separator+"ReceiveMail");
		Util.mkdirs(file);
		file = new File(file, File.separator+Util.getMailName());//文件路径
		BufferedWriter bw = Util.getBufferedWriter(file);//输出流
		String subMail = null;
		try {
			while(!(subMail=Util.getInput(br)).equals(".")){
					bw.write(subMail);
					bw.newLine();
					bw.flush();
			}
			bw.write(".");
			bw.close();
			//回复邮件接收成功
			Util.println(ps, "250 Mail OK ");
			return "ok";
			
		} catch (IOException e) {
//			e.printStackTrace();
			return "NullPointerException";
		}
//		return "ok";
	}


	//发送外域邮件过程     单件
	public String sendForeignMail(String foreSendMailer, String foreReceMailer){
		//获取收件人的邮件服务商信息
//		String[] recips = recipient.split("/");
		//获取recipient的邮件提供商类型，再socket
		int at = foreReceMailer.indexOf("@");
		int l = foreReceMailer.indexOf(">");
		String mailSupplier = foreReceMailer.substring((at+1), l);
		System.err.println("mailSupplier:"+mailSupplier);
		
		foreignMap = OnceClass.getOnce().getForeignServiceMap();
		ForeignServiceBean foreignService = foreignMap.get(mailSupplier);
		//服务器的MX记录
		String smtpMx = foreignService.getStmpMx();
		System.err.println(smtpMx);
		
		int smtpPort = Integer.parseInt(foreignService.getStmpPort());
		try {
			//连接邮件提供商的SMTP服务器
			Socket clientSocket = new Socket(smtpMx, smtpPort);
			//获取输入输出流
			BufferedReader foreignBR = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintStream foreignPS = new PrintStream(clientSocket.getOutputStream());
			String returnMess = foreignBR.readLine();//读取服务器的消息
				System.out.println("邮件服务器的返回信息：linkOK:"+returnMess);
			if(!returnMess.contains("220")){//如果不包含220，表示服务器未就绪。处理信息。
				
			}
			foreignPS.println("ehlo smtp.57901.com");
//				System.out.println("ehlo smtp.57901.com");
			int count;
			if(smtpMx.contains("qq"))
				count=3;
			else if(smtpMx.contains("sina"))
				count = 2;
			else if(smtpMx.contains("163"))
				count = 6;
			else 
				count = 3;
			for(int i=0; i<count; i++){//163读取6行返回数据//qq读取3行数据//新浪返回2行
				returnMess = foreignBR.readLine();//250 ok
//					System.out.println(returnMess+"   ehlo>回复");
			}
			foreignPS.println(foreSendMailer);//mail from:<xiexianbin@163.com>
			returnMess = foreignBR.readLine();//250 Mail OK
//				System.err.println("邮件服务器的返回信息："+returnMess);
			foreignPS.println(foreReceMailer);//rcpt to:<xianbinxie@163.com>
			returnMess = foreignBR.readLine();//250 Mail OK
//				System.err.println("邮件服务器的返回信息："+returnMess);
			
			//存放命令
			String order = "data";
			//传送data
			foreignPS.println(order);//data
			returnMess = foreignBR.readLine();//354 End data with <CR><LF>.<CR><LF>
//				System.err.println("邮件服务器的返回信息："+returnMess);
			String subForMess = null;
			while((subForMess=br.readLine())!=null && !subForMess.equals(".") && !subForMess.equals("quit") & !subForMess.equals("QUIT")){
				foreignPS.println(subForMess);
//				System.out.println(subForMess);
				foreignPS.flush();
			}
			foreignPS.println(".");
			foreignPS.flush();
			returnMess = foreignBR.readLine();
//				System.err.println("邮件服务器的返回信息："+returnMess);
			if(returnMess.contains("250")){
				ps.println("250 Mail OK");
				//与邮件服务商连接断开
				foreignPS.println("quit");
				return "ok";
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return "NullPointerException";
			
		}
		return "ok";
	}
	
	
	
	
	//发送邮件本地邮件过程    
	public String sendLocalMail(String sender, String recipient){
		//开始传输数据，判断是否以.结束
		String subMail = null;
		//获取once
		once = OnceClass.getOnce();
		//获取文件的默认存放路径
		localService = once.getLocalServiceInfo();
		String mailPath = localService.getMailPath();

		//保存到发件人的发件文件夹下
		File sendFile = new File(mailPath+File.separator+sender+File.separator+"SendMail");//保存到发件人的 已发送邮件列表 下
		Util.mkdirs(sendFile);
		sendFile = new File(sendFile, File.separator+Util.getMailName()+".eml");
		Util.creatNewFile(sendFile);
		BufferedWriter sendBW = Util.getBufferedWriter(sendFile);
		String[] recips = recipient.split("/");
		//获取群发接受用户的bw
		BufferedWriter[] reciBW = new BufferedWriter[recips.length];
		File[] reFile = new File[recips.length];
		for(int i=0; i<recips.length; i++){
			File reciFile = new File(mailPath+File.separator+recips[i]+File.separator+"ReceiveMail");
			Util.mkdirs(reciFile);
			reciFile = new File(reciFile, File.separator+Util.getMailName()+sender+".eml");
			reFile[i] = reciFile;
			Util.creatNewFile(reciFile);
			reciBW[i] = Util.getBufferedWriter(reciFile);
		}
		//把读取到的邮件内容写入分别写入到**.eml文件中
		//quit的问题
		try {
			while(((subMail=br.readLine())!=null) && !subMail.equals(".") && !subMail.equals("QUIT") && !subMail.equals("quit")){
				//写入发件人的文件中
				sendBW.write(subMail);
				sendBW.newLine();
				sendBW.flush();
				//保存到收件人的收件文件夹下
				for(int i=0; i<recips.length; i++){
					reciBW[i].write(subMail);
					reciBW[i].newLine();
					reciBW[i].flush();
				}
			}
			for(int i=0; i<recips.length; i++){
				reciBW[i].close();
			}
			sendBW.close();
			if(subMail == null){
				//连接异常关闭，错误，
//				Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
				return "NullPointerException";
			}
			if(subMail.equals("QUIT") || subMail.equals("quit")){
				doQuit();
//				Util.saveLog(Util.getClientInfo(client)+"与服务器连接正常退出。时间："+Util.getNowTime());
				return "quit";
			}
			//回复邮件传送成功
			overData();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Util.saveLog(Util.getClientInfo(client)+"与SMTP服务器连接异常中断。");
			for(int i=0; i<reFile.length; i++){
				try {
					reciBW[i].close();
					reFile[i].delete();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			return "NullPointerException";
		}
		//正常结束，返回true。
		return "ok";
	}
	
	//登录成功
	private void doLoginOK() {
		// TODO Auto-generated method stub
		Util.print(ps, "235 #2.0.0 OK Authenticated");
	}
	//相应错误的command
	public void doWrongCommand(){
		Util.println(ps, "500 #5.5.1 command not recognized");
	}
	//相应data命令
	public void responseData(){
		Util.println(ps, "354 End data with .");
	}
	//data命令执行成功时的相应
	public void overData(){
		Util.println(ps, "250 OK: mail sent success");
	}
	//关闭服务器 //say bye
	private void doQuit() {
		// TODO Auto-generated method stub
		Util.println(ps, "221 Bye");
		Util.println(ps, "Connection closed by foreign host.");
	}
	//服务器相应ok
	private void doOk() {
		// TODO Auto-generated method stub
		Util.println(ps, "250 OK");
	}
	//输入telnet时提示信息
	public void startLinkOk(){
		Util.println(ps, "Trying "+Util.getLocalHostIp()+"...");
		Util.println(ps, "Connected to "+Util.getLocalHostIp()+".");
		Util.println(ps, "220 "+Util.getLocalHostIp()+" ESMTP Postfix - by "+OnceClass.getOnce().getLocalServiceInfo().getMailSuffix());
	}
}
