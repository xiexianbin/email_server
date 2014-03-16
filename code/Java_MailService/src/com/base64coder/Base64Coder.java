package com.base64coder;

import java.io.IOException;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

public class Base64Coder {

	//加密
	public static String enCoder(String str){
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(str.getBytes());
	}
	//解密
	public static String deCoder(String str){
		BASE64Decoder decoder = new BASE64Decoder();
		String okcoder = "";	//把okcoder初始化为""，我别有深意。
		try {
			okcoder = new String(decoder.decodeBuffer(str));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return okcoder;
	}
}
