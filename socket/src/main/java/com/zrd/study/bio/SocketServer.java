package com.zrd.study.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
	public static void main(String[] args) throws IOException {
		try(
				ServerSocket serverSocket = new ServerSocket(81)){
			System.out.println("启动服务器...");
			while(true){
				Socket socket = serverSocket.accept();
				
				try(
						InputStream in = socket.getInputStream();
						OutputStream out = socket.getOutputStream();
					){
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
					
					System.out.println("收到信息：" + "[" + socket.getInetAddress().getHostAddress() + "]" + message);
					
					out.write("我已收到你的信息".getBytes());
				}
				
			}
		}
	}
}
