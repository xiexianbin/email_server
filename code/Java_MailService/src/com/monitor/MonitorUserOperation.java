package com.monitor;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;

import com.bean.UserBean;
import com.onceClass.OnceClass;
import com.util.Util;

public class MonitorUserOperation {

	private PrintStream ps;
	private BufferedReader br;
	private Socket socket;
	private String mailSuffix;
	public MonitorUserOperation(PrintStream ps, BufferedReader br, Socket socket) {
		// TODO Auto-generated constructor stub
		this.ps = ps;
		this.br = br;
		this.socket = socket;
	}
	
	private OnceClass once;
	//登入成功后的主菜单
	public String loginSuccess(String userName) {
		once = OnceClass.getOnce();
		while(true){
			// TODO Auto-generated method stub
			Util.printlns(ps, "主菜单：1.注销邮箱\t2.修改密码\t3.退出\t其他重新输入");
			String subMenu = Util.getInput(br);
			if(subMenu == null){
				return "NullPointerException";	
			}
			if(subMenu.equals("1")){
				UserBean user = once.getUserMap().get(userName);
				Util.printlns(ps, userName+"注册于"+user.getUserRegedtiDate()+"是否注销(y/n)");
				String choice = Util.getInput(br);
				if(choice == null){
					return "NullPointerException";	
				}
				if(choice.equals("y")){
					once.getUserMap().remove(userName);
					Util.write2UserMapFile();
					Util.saveLog("邮箱"+userName+"注销成功。时间："+Util.getNowTime());
					Util.println(ps, "邮箱"+userName+"注销成功，系统将于3秒后退出。");
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return "Exit";
				}else if(choice.equals("n")){
					Util.println(ps, "返回上级");
				}else{
					continue;
				}
		
			}else if(subMenu.equals("2")){
				//修改密码
				Util.printlns(ps, "请输入当前密码：");
				String pass = Util.getInput(br);
				if(pass == null){
					return "NullPointerException";	
				}
				//获取密码匹配
				UserBean user = once.getUserMap().get(userName);
				if(user.getUserPass().equals(pass)){
					Util.printlns(ps, "请输入新密码：");
					String newPass = Util.getInput(br);
					if(newPass == null){
						return "NullPointerException";	
					}
					user.setUserPass(newPass);
					//写入文件
					Util.write2UserMapFile();
					Util.println(ps, "设置成功。");
				}else{
					Util.println(ps, "密码输入错误。返回上级。");
				}
			}else if(subMenu.equals("3")){
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
	//注册新用户
	public boolean regeditNewUser() {
		// TODO Auto-generated method stub
		Map<String, UserBean> userMap;
		mailSuffix = OnceClass.getOnce().getLocalServiceInfo().getMailSuffix();
		once = OnceClass.getOnce();
		Util.printlns(ps, "请输入用户名(不包含"+mailSuffix+")部分：");
		String userName = Util.getInput(br);
		if(userName == null){
			return false;
		}
		userName = userName.toLowerCase();
		userMap = once.getUserMap();
		if(!userMap.containsKey(userName)){
			Util.printlns(ps, "请输入用户密码:");
			String userPass = Util.getInput(br);
			if(userPass == null){
				return false;
			}
			Util.printlns(ps, "请输入密保问题:");
			String userQuestion = Util.getInput(br);
			if(userQuestion == null){
				return false;
			}
			Util.printlns(ps, "请输入密保答案:");
			String userAnswer = Util.getInput(br);
			if(userAnswer == null){
				return false;
			}
			UserBean user = new UserBean((userName+mailSuffix), userPass, userQuestion, userAnswer, Util.getNowTime());
			userMap.put((userName+mailSuffix), user);
			Util.println(ps, userName+"注册成功。请登录。");
			//写入map中
			Util.write2UserMapFile();
			Util.saveLog((userName+mailSuffix)+"在"+Util.getClientInfo(socket)+"注册成功。");
		}else{
			Util.println(ps, "用户名已存在，请登录。");
		}
		
		return true;
	}
	//找回密码
	public boolean findUserPass() {
		// TODO Auto-generated method stub
		Util.printlns(ps, "请输入完整用户名:");
		String userName = Util.getInput(br);
		if(userName == null){
			return false;
		}
		userName = userName.toLowerCase();
		//获取userMap
		Map<String, UserBean> userMap;
		once = OnceClass.getOnce();
		userMap= once.getUserMap();
		if(userMap.containsKey(userName)){
			//用户存在，找回
			UserBean user = userMap.get(userName);
			Util.printlns(ps, "您的密保问题是："+user.getUserAnswer()+" 请输入密保答案：");
			String question = Util.getInput(br);
			if(question == null){
				return false;
			}
			if(question.equals(user.getUserQuestion())){
				Util.printlns(ps, "请输入新密码：");
				String newPass = Util.getInput(br);
				if(newPass == null){
					return false;
				}
				user.setUserPass(newPass);
				Util.println(ps, userName+"修改密码成功，请登录。");
				Util.saveLog(userName+"在"+Util.getNowTime()+"修改密码成功。");
				return true;
			}else{
				Util.println(ps, "密码输入错误，返回上级。");
				return true;
			}
		}else{
			//用户不存在
			Util.println(ps, "您输入的用户名不存在，返回上级。");
			return true;
		}
	}
}
