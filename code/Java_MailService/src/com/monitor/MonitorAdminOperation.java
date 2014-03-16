package com.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.bean.ForeignServiceBean;
import com.bean.LocalServiceBean;
import com.bean.UserBean;
import com.onceClass.OnceClass;
import com.receiveMail.ReceiveMailService;
import com.receiveMail.ReceiveMailThread;
import com.sendMail.SendMailService;
import com.service.main.MailServiceMain;
import com.util.Util;

public class MonitorAdminOperation {

	/**
	 * 管理员的相关操作：
	 * 		1.设置服务器端口号
	 * 		2.设置邮件的接收路径
	 *		3.设置邮件服务的状态
	 * 		4.设置该邮件系统的后缀
	 */
	private PrintStream ps;
	private BufferedReader br;
	private Socket socket;
	public MonitorAdminOperation(PrintStream ps, BufferedReader br, Socket socket) {
		// TODO Auto-generated constructor stub
		this.ps = ps;
		this.br = br;
		this.socket = socket;
	}
	
	private OnceClass once;
	private LocalServiceBean localServiceInfo;
	//登入成功后的主菜单
	public String adminLoginSuccess(String userName) {
		once = OnceClass.getOnce();
		while(true){
			// TODO Auto-generated method stub
			Util.printlns(ps, "主菜单：1.设置服务器端口号\t2.设置邮件的接收路径\t3.设置邮件服务的状态\r\n\t4.设置该邮件系统的后缀\t5.设置其他邮件提供商的信息\r\n\t6.查看邮箱用户\t7.退出\t其他重新输入");
			String subMenu = Util.getInput(br);
			if(subMenu == null){
				return "NullPointerException";	
			}
			if(subMenu.equals("1")){
				while(true){
					//获取本地的
					
					//设置服务器的端口号
					Util.printlns(ps, "子菜单：1.设置STMP端口号\t2.设置POP3端口号\t其他返回上级");
					String subsubMenu = Util.getInput(br);
					if(subsubMenu == null){
						return "NullPointerException";
					}
//////////////////////////////////端口号被占用未处理？？？？？？？？？？？？
					localServiceInfo = once.getLocalServiceInfo();
					if(subsubMenu.equals("1")){
						//设置邮件发送端口号
						
						Util.println(ps, "当前邮件发送端口号："+localServiceInfo.getStmpPort());
						Util.printlns(ps, "请输入新的端口号:(整数)");
						String newSentPort = Util.getInput(br);
						if(newSentPort == null){
							return "NullPointerException";
						}
//						int newPort = Integer.parseInt(newSentPort);
						localServiceInfo.setStmpPort(newSentPort);
						//写入文件
						Util.write2LocalServiceFile();
						Util.println(ps, "设置STMP端口成功，重启服务器生效。");
					}else if(subsubMenu.equals("2")){
						//设置邮件接收端口号
						Util.println(ps, "当前邮件接收端口号："+localServiceInfo.getPop3Port());
						Util.printlns(ps, "请输入新的端口号:(整数)");
						String newReceivePort = Util.getInput(br);
						if(newReceivePort == null){
							return "NullPointerException";
						}
//						int newPort = Integer.parseInt(newReceivePort);
						
						localServiceInfo.setPop3Port(newReceivePort);
						//写入文件
						Util.write2LocalServiceFile();
						Util.println(ps, "设置POP3成功，重启生效。");
					}else{
						break;
					}
				}
			}else if(subMenu.equals("2")){
				//设置用户的邮件接收路径
				localServiceInfo = once.getLocalServiceInfo();
				Util.printlns(ps, "当前邮件的默认路径为："+localServiceInfo.getMailPath()+"请输入新的路径：");
				String mailPath = Util.getInput(br);
				if(mailPath == null){
					return "NullPointerException";
				}
				//判断是否为路径
				File mailFile = new File(mailPath);
				if(mailFile.isAbsolute() && !mailFile.isFile()){
					//判断是否存在，若不存在，则创建。
					if(!mailFile.exists()){
						mailFile.mkdirs();
					}
					//保存到user
					localServiceInfo.setMailPath(mailPath);
					//写入文件
					Util.write2LocalServiceFile();
					Util.println(ps, "设置邮件路径成功，重启生效。");
				}else{
					Util.println(ps, "您输入的不是一个路径，配置失败。");
				}
				
			}else if(subMenu.equals("3")){
				/**
				 * 设置服务器的运行状态:
				 * 		正常：running
				 * 		停止：shutdown
				 *	最后写入文件
				 */
				once = OnceClass.getOnce();
				localServiceInfo = once.getLocalServiceInfo();
				while(true){
					localServiceInfo = once.getLocalServiceInfo();
					Util.println(ps, "当前服务器的运行状态："+localServiceInfo.getServiceState());
					Util.printlns(ps, "菜单：1.设置运行/停止\t2.重启服务器\t3.返回上级\t其它重新输入");
					String subMenu3 = Util.getInput(br);
					if(subMenu3 == null){
						return "NullPointerException";
					}
					if(subMenu3.equals("1")){
						//设置服务器的运行状态，若当前为运行，则设为停止。
						once = OnceClass.getOnce();
						Map<String, ServerSocket> serverSocketMap = once.getServerSocketMap();
						if(localServiceInfo.getServiceState().equals("running")){
							localServiceInfo.setServiceState("shutdown");
							Util.println(ps, "5s后，POP3服务器停止运行...");
							try {
								Thread.sleep(5000);
								serverSocketMap.get("receiveMailServerSocket").close();
								serverSocketMap.remove("receiveMailServerSocket");
								Util.println(ps, "POP3服务器已停止运行。端口号："+localServiceInfo.getPop3Port()+"，服务器时间："+Util.getNowTime());
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Util.println(ps, "5s后，SMTP服务器停止运行...");
							try {
								Thread.sleep(5000);
								serverSocketMap.get("sendMailServerSocket").close();
								serverSocketMap.remove("sendMailServerSocket");
								Util.println(ps, "SMTP服务器已停止运行。端口号："+localServiceInfo.getStmpPort()+"，服务器时间："+Util.getNowTime());
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							Util.write2LocalServiceFile();
						}else if(localServiceInfo.getServiceState().equals("shutdown")){
							localServiceInfo.setServiceState("running");
							Util.write2LocalServiceFile();
							//启动POP3服务器
							ReceiveMailService rms = new ReceiveMailService();
							Thread rmsTh = new Thread(rms);
							rmsTh.start();
							Util.println(ps, "POP3服务器启动成功。端口号为："+localServiceInfo.getPop3Port()+"，时间为："+Util.getNowTime());
							//启动SMTP服务器
							SendMailService sms = new SendMailService();
							Thread smsTh = new Thread(sms);
							smsTh.start();
							Util.println(ps, "SMTP服务器启动成功。端口号为："+localServiceInfo.getStmpPort()+"，时间为："+Util.getNowTime());
						}else{
							localServiceInfo.setServiceState("running");
							Util.write2LocalServiceFile();
						}
						//Util.println(ps, "设置成功，重启生效！");
					}else if(subMenu3.equals("2")){
						/**重启服务器
						 * 		1. 在主函数中，先重启SMTP服务器和POP3服务器的运行标志位 flag为false。
						 * 		2. 当启动一个线程后，把信息put到相应map
						 * 		3. 设置服务器的运行状态为true。
						 * 		4. 重启SMTP服务器和POP3服务器。
						 * 本次方法：
						 * 		1. 
						*/
						//
						SendMailService send = new SendMailService();
						ReceiveMailService rece = new ReceiveMailService();
						
						if(once.getServerSocketMap().size() != 0){
							int oldSMTPPort = once.getServerSocketMap().get("sendMailServerSocket").getLocalPort();
							int oldPOP3Port = once.getServerSocketMap().get("receiveMailServerSocket").getLocalPort();
							int newPOP3Port = Integer.parseInt(once.getLocalServiceInfo().getPop3Port());
							int newSMTPPort = Integer.parseInt(once.getLocalServiceInfo().getStmpPort());
							if(oldSMTPPort == newSMTPPort && oldPOP3Port == newPOP3Port){
								//服务器参数未变更，重启失败
								Util.println(ps, "服务器参数未变更，重启失败");
								continue;
							}
						
							//分别关闭两个服务器
							once = OnceClass.getOnce();
							System.err.println("当前系统的线程数："+Thread.activeCount()+"。");
							Util.println(ps, "系统重启中...");
							if(newPOP3Port != oldPOP3Port){
								//关闭线程
	//							rece.setPOP3ServiceState();
								once = OnceClass.getOnce();
								Map<String, ServerSocket> serverSocketMap = once.getServerSocketMap();
								try {
//									System.out.println(serverSocketMap.get("receiveMailServerSocket"));
									Util.println(ps, "5s后，POP3服务器重启！");
									Thread.sleep(5000);
									serverSocketMap.get("receiveMailServerSocket").close();
									serverSocketMap.remove("receiveMailServerSocket");
									Util.println(ps, "POP3服务器重启成功。端口号："+localServiceInfo.getPop3Port()+"，服务器时间："+Util.getNowTime());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								ReceiveMailService rms = new ReceiveMailService();
								Thread rmsTh = new Thread(rms);
								rmsTh.start();
//								rece.setPOP3ServiceState();
//								rms = new ReceiveMailService();
//								rmsTh = new Thread(rms);
//								rmsTh.start();
							}
							if(newSMTPPort != oldSMTPPort){
								//关闭线程
	//							send.setSMTPServiceState();
								once = OnceClass.getOnce();
								Map<String, ServerSocket> serverSocketMap = once.getServerSocketMap();
								try {
//									System.out.println(serverSocketMap.get("sendMailServerSocket"));
									Util.println(ps, "5s后，SMTP服务器重启！");
									Thread.sleep(5000);
									serverSocketMap.get("sendMailServerSocket").close();
									serverSocketMap.remove("sendMailServerSocket");
									Util.println(ps, "SMTP服务器重启成功。端口号："+localServiceInfo.getStmpPort()+"，服务器时间："+Util.getNowTime());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									System.err.println("重启POP3服务器。");
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								SendMailService sms = new SendMailService();
								Thread smsTh = new Thread(sms);
								smsTh.start();
//								send.setSMTPServiceState();
//								sms = new SendMailService();
//								smsTh = new Thread(sms);
//								smsTh.start();
								
							}
//							Util.saveLog("重启命令发送完毕，"+Util.getNowTime());
							
		
						}else{
							Util.println(ps, "服务器未启动，重启失败！");
						}
						
					}else if(subMenu3.equals("3")){
						//返回上级
						break;
					}else{
						//重新输入、
						continue;
					}
				}
			}else if(subMenu.equals("4")){
				localServiceInfo = once.getLocalServiceInfo();
//				System.out.println("localServiceInfo:"+localServiceInfo);
				//设置邮件系统的域名后缀
				Util.printlns(ps, "请输入邮件系统的域名后缀为："+localServiceInfo.getMailSuffix()+"请输入新后缀：(如：@mail.com)");
				String mailSuffix = Util.getInput(br);
				if(mailSuffix == null){
					return "NullPointerException";	
				}
				mailSuffix = mailSuffix.trim();
				//获取邮件系统的基本信息
				localServiceInfo = once.getLocalServiceInfo();
				if(mailSuffix.startsWith("@")){
					//设置
					localServiceInfo.setMailSuffix(mailSuffix);
					//保存
					Util.write2LocalServiceFile();
					Util.println(ps, "ok");
				}else{
					Util.println(ps, "输入错误，返回上级。");
				}
			}else if(subMenu.equals("5")){
				while(true){
					once = OnceClass.getOnce();
					Util.printlns(ps, "菜单：1.添加\t2.查看\t3.删除\t4.返回上级");
					String choice = Util.getInput(br);
					if(choice == null){
						return "NullPointerException";
					}
					if(choice.equals("1")){
						Map<String, ForeignServiceBean> foreignServiceMap = once.getForeignServiceMap();
						//添加
						Util.printlns(ps, "请输入服务商名称：");
						String serviceName = Util.getInput(br);
						if(serviceName == null){
							return "NullPointerException";
						}
						Util.printlns(ps, "请输入SMTP_MX:");
						String smtp_mx = Util.getInput(br);
						if(smtp_mx == null){
							return "NullPointerException";
						}
						Util.printlns(ps, "请输入SMTP_PORT:");
						String smtp_port = Util.getInput(br);
						if(smtp_port == null){
							return "NullPointerException";
						}
						
						ForeignServiceBean fsb = new ForeignServiceBean(serviceName, smtp_mx, smtp_port);
						Util.saveLog("添加其他邮件提供商："+serviceName+"\t"+smtp_mx+"\t"+smtp_port+"成功。");
						foreignServiceMap.put(serviceName, fsb);
						Util.write2ForeignServiceFile();
						Util.println(ps, "ok");

					}else if(choice.equals("2")){
						Map<String, ForeignServiceBean> foreignServiceMap = once.getForeignServiceMap();
						if(foreignServiceMap.size()!=0){
							Util.println(ps, "服务商\tSMTP_MX\t\tSMTP_PORT");
							//查看
							Set<String> keySet = foreignServiceMap.keySet();
							Iterator<String> it = keySet.iterator();
							while(it.hasNext()){
								String key = it.next();
								ForeignServiceBean fsb = foreignServiceMap.get(key);
								Util.println(ps, fsb.getServiceName()+"\t"+fsb.getStmpMx()+"\t"+fsb.getStmpPort());
							}
							Util.println(ps, "\n扫描完成。");
						}else{
							Util.println(ps, "不存在其他服务器提供商的基本信息，请录入。");
						}
						
					}else if(choice.equals("3")){
						//删除
						Util.printlns(ps, "请输入要删除的服务商名称：");
						String serviceName = Util.getInput(br);
						if(serviceName == null){
							return "NullPointerException";
						}
						once = OnceClass.getOnce();
						if(once.getForeignServiceMap().containsKey(serviceName)){
							once.getForeignServiceMap().remove(serviceName);
							Util.write2ForeignServiceFile();
							Util.println(ps, "ok");
						}else{
							Util.println(ps, "输入错误，不存在该邮件提供商的信息");
						}
					}else{
						//返回上级
						break;
					}
				}
			}else if(subMenu.equals("6")){
				once= OnceClass.getOnce();
				Map<String, UserBean> userMap = once.getUserMap();
				Set<String> userSet = userMap.keySet();
				Object[] ob = userSet.toArray();
				Util.println(ps, "当前系统的用户：");
				for(int i=0; i<ob.length; i++){
					Util.println(ps, (String)ob[i]);
				}
				Util.println(ps, "扫描完成！");
				
			}else if(subMenu.equals("7")){
				//退出
				Util.println(ps, "此系统将于3秒后退出");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Util.println(ps, "\n\t退出成功。欢迎下次使用。");
				return "Exit";
			}else{
				continue;
			}
			//未知错误，退出。
		}
//		return "NullPointerException";
	}
}
