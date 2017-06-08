package com.zrd.study.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {
	public static void main(String[] args) throws UnknownHostException, IOException {
		try(
				Socket socket = new Socket("127.0.0.1", 81);
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();
			){
			out.write("我是客户端\\r\\n".getBytes());
			
			int maxLen = 2048;
			byte[] conetextBytes = new byte[maxLen];
			int realLen;
			StringBuffer message = new StringBuffer();
			
			while((realLen = in.read(conetextBytes, 0, maxLen)) != -1){
				message.append(new String(conetextBytes, 0, realLen));
				
				if(message.indexOf("\\r\\n") != -1){
					break;
				}
			}
			
			System.out.println("收到服务器信息：" + "[" + socket.getInetAddress().getHostAddress() + "]" + message);
		}
	}
}
