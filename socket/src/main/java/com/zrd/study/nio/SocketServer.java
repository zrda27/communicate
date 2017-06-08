package com.zrd.study.nio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SocketServer {
	private static Object xWait = new Object();
	
	public static void main(String[] args) throws IOException, InterruptedException {
		try(
				ServerSocket serverSocket = new ServerSocket(82)){
			System.out.println("启动服务器...");
			serverSocket.setSoTimeout(1000);
			while(true){
				Socket socket = null;
				try{
					socket = serverSocket.accept();
				}catch (SocketTimeoutException e) {
					System.out.println("等了1s没数据");
					
					synchronized (xWait) {
						xWait.wait(1000);
					}
					continue;
				}
				socket.setSoTimeout(1000);
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
				}catch (SocketTimeoutException e) {
					System.out.println("读取数据超时");
				}
				
			}
		}
	}
}
