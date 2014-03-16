package com.monitor;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.bean.UserBean;
import com.onceClass.OnceClass;
import com.util.Util;

public class MonitorSubThread implements Runnable {

	//传入用户的客户端套接字。获取相应的输入输出流。
	private Socket clientSocket;
	private PrintStream ps;
	private BufferedReader br;
	//定义该进程的运行标识。
	private boolean flag = true;
	//声明用户操作类对象
	private MonitorUserOperation mo;
	private MonitorAdminOperation ao;
	//构造器，用户传入客户端套接字，并获取相应的输入/出流。
	public MonitorSubThread(Socket clientSocket){
		this.clientSocket = clientSocket;
		ps = Util.getPrintStream(clientSocket);
		br = Util.getBufferedReader(clientSocket);
	}
	private OnceClass once ;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//获取客户段套接字信息。
		String clientInformation = Util.getClientInfo(clientSocket);
		Util.saveLog(clientInformation+"连接服务器成功。当前进程的活动线程数："+Thread.activeCount());
		//实例化登录成功后，用户操作的方法对象
		mo = new MonitorUserOperation(ps, br, clientSocket);
		ao = new MonitorAdminOperation(ps, br, clientSocket);
		once = OnceClass.getOnce();
		//获取单子类
		while(flag){
			String mailSuffix = once.getLocalServiceInfo().getMailSuffix();
			Util.println(ps, "**********************"+once.getLocalServiceInfo().getMailSuffix().replace("@", "")+"邮件系统***************************");
			Util.printlns(ps, "请选择用户身份：1.邮箱用户\t2.服务器管理员\t3.退出\t其它重新输入");
			String choiceMenu = Util.getInput(br);
			if(choiceMenu == null){
				Util.saveLog(clientInformation+"与服务器连接异常中断。");
				return ;
			}
			if(choiceMenu.equals("1")){
				while(flag){
					//邮箱用户
					Util.printlns(ps, "菜单：1.登录\t2.注册\t3.找回密码\t4.返回上级\t其它重新输入");
					String menu = Util.getInput(br);
					//若menu为null，则用户退出，结束该进程。
					if(menu == null){
						Util.saveLog(clientInformation+"在邮箱登录界面与服务器连接异常中断。");
						return ;
					}
					if(menu.equals("1")){
						//登录
						Util.printlns(ps, "请输入用户名:");
						String userName = Util.getInput(br);
						if(userName == null){
							Util.saveLog(clientInformation+"输入用户名时与服务器连接异常中断。");
							return ;
						}
						userName = userName.toLowerCase();
						//获取userMap
						Map<String, UserBean> userMap;
						once = OnceClass.getOnce();
						userMap= once.getUserMap();
						if(userMap.containsKey(userName) || userMap.containsKey(userName+mailSuffix)){
							if(!userName.contains(mailSuffix))
								userName += mailSuffix;
							Util.printlns(ps, "请输入密码:");
							String userPass = Util.getInput(br);
							if(userPass == null){
								Util.saveLog(clientInformation+"输入用户密码时与服务器连接异常中断。");
								return ;
							}
							//获取onLineMap
							Map<String, Socket> onLineMap = once.getOnLineMap();
							//匹配密码
							if(userMap.get(userName).getUserPass().equals(userPass)){
								if(!onLineMap.containsKey(userName)){
									//登录成功
									Util.saveLog(userName+"在"+clientInformation+"登入服务器成功。");
									Util.println(ps, userName+"在"+Util.getNowTime()+"登入成功。");
									//把用户的在线状态添加到onLineMap集合
									onLineMap.put(userName, clientSocket);
									//进入登入成功后的主菜单
									String runState = mo.loginSuccess(userName);
									
									if(runState.equals("NullPointerException"))
									{
										//删除已登出用户
										Util.deleLogOutUser(userName);
										Util.saveLog(userName+"在"+clientInformation+"操作失误，与服务器连接异常中断。");
										return ;
									}else if(runState.equals("Exit")){
										//删除已登出用户
										Util.deleLogOutUser(userName);
										Util.saveLog(userName+"在"+clientInformation+"成功登出。");
										continue;
									}
								}else{
									//提示异常登录信息
									Socket loginedSocket = onLineMap.get(userName);
									//输出流
									PrintStream loginedPS = Util.getPrintStream(loginedSocket);
									String message = userName+"在"+Util.getClientInfo(clientSocket)+"异常登录失败。";
									Util.printlns(loginedPS, "重要提示："+message);
									String message2 = Util.getClientInfo(loginedSocket);
									//后登陆端提示
									Util.printlns(ps, userName+"在"+message2+"登录");
									//保存日志
									Util.saveLog(message+"当前登录为："+message2);
								}
							}else{
								Util.println(ps, "密码输入错误，反回菜单。");
								continue;
							}
						}else{
							Util.println(ps, "用户名不存在，请先注册。");
							continue;
						}
					}else if(menu.equals("2")){
						//注册
						boolean fl = mo.regeditNewUser();
						if(!fl){
							Util.saveLog(clientInformation+"注册新用户时与服务器连接异常中断，注册失败。");
							return ;
						}
					}else if(menu.equals("3")){
						//找回密码
						boolean fl = mo.findUserPass();
						if(!fl){
							Util.saveLog(clientInformation+"用户找回密码时与服务器连接异常中断。");
							return ;
						}
						
					}else if(menu.equals("4")){
						//返回上级
						break;
					}else{
						//重新输入
						continue;
					}
				}
				
			}else if(choiceMenu.equals("2")){
				//服务器管理员
				while(flag){
					Util.printlns(ps, "菜单：1.登录\t2.返回上级\t其它重新输入");
					String menu = Util.getInput(br);
					//若menu为null，则用户退出，结束该进程。
					if(menu == null){
						Util.saveLog(clientInformation+"在管理员登录界面与服务器连接异常中断。");
						return ;
					}
					if(menu.equals("1")){
						//登录
						Util.printlns(ps, "请输入管理员帐号:");
						String adminName = Util.getInput(br);
						if(adminName == null){
							Util.saveLog(clientInformation+"输入管理员帐号时与服务器连接异常中断。");
							return ;
						}
						if(adminName.equals(Util.ADMIN)){
							Util.printlns(ps, "请输入管理员密码:");
							String adminPass = Util.getInput(br);
							if(adminPass == null){
								Util.saveLog(clientInformation+"输入管理员密码时与服务器连接异常中断。");
								return ;
							}
							if(adminPass.equals(Util.ADMINPASS)){
								//登录成功
								Util.saveLog(adminName+"在"+clientInformation+"登入服务器成功。");
								Util.println(ps, adminName+"在"+Util.getNowTime()+"登入成功。");
								String runState = ao.adminLoginSuccess(adminName);
								if(runState.equals("NullPointerException"))
								{
									Util.saveLog(adminName+"在"+clientInformation+"操作失误，与服务器连接异常中断。");
									return ;
								}else if(runState.equals("Exit")){
									Util.saveLog(adminName+"在"+clientInformation+"成功登出。");
									continue;
								}else{
									Util.println(ps, "密码输入错误，反回菜单。");
									continue;
								}
							}else{
								Util.println(ps, "该管理员账户不存在，请重新输入。");
								continue;
							}
						}
					}else if(menu.equals("2")){
						//返回上级
						break;
					}else{
						//重新输入
						continue;
					}
				}
			}else if(choiceMenu.equals("3")){
				//退出访问
				Util.saveLog(clientInformation+"连接正常退出。");
				Util.println(ps, "\n\n\t已于服务器成功断开连接。请关闭该窗口。谢谢使用。");
				return;
			}else{
				//重新输入
				continue;
			}
		}
	}
}
