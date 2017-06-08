package com.zrd.study.multiio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SocketServer {
	private static Map<Integer, StringBuffer> textMap = new HashMap<>();
	
	public static void main(String[] args) throws IOException, InterruptedException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		ServerSocket serverSocket = serverChannel.socket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(new InetSocketAddress(83));
		
		Selector selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		while(true){
			if(selector.select(5000) == 0){
				System.out.println("等了5秒");
				
				continue;
			}
			
			Iterator<SelectionKey> selectionKeyIter = selector.selectedKeys().iterator();
			
			while(selectionKeyIter.hasNext()){
				SelectionKey selectionKey = selectionKeyIter.next();
				selectionKeyIter.remove();
				
				SelectableChannel selectableChannel = selectionKey.channel();
				if(selectionKey.isValid() && selectionKey.isAcceptable()){
					System.out.println("开始接受数据");
					
					SocketChannel socketChannel = ((ServerSocketChannel)selectableChannel).accept();
					socketChannel.configureBlocking(false);
					socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(2048));
					
					System.out.println("acceptuuid:" + socketChannel.hashCode());
				}else if(selectionKey.isValid() && selectionKey.isConnectable()){
					System.out.println("建立连接");
				}else if(selectionKey.isValid() && selectionKey.isReadable()){
					System.out.println("开始读数据");
					readSocketChannel(selectionKey);
					
				}
			}
		}
	}
	
	private static void readSocketChannel(SelectionKey selectionKey) throws IOException{
		SocketChannel clientSocketChannel = (SocketChannel) selectionKey.channel();
		System.out.println("来自客户端请求，ip=" + clientSocketChannel.getRemoteAddress());
		System.out.println("readUUID:" + clientSocketChannel.hashCode());
		
		ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
		
		int realLen = -1;
		try{
			realLen = clientSocketChannel.read(buffer);
		}catch (Exception e) {
			e.printStackTrace();
			clientSocketChannel.close();
			return;
		}
		
		buffer.flip();
		
		String message = URLDecoder.decode(new String(buffer.array(), "UTF-8"), "UTF-8");
		buffer.clear();
		if(message.indexOf("\\r\\n") != -1){
			
			String completeMessage;
            //清空MESSAGEHASHCONTEXT中的历史记录
            StringBuffer historyMessage = textMap.remove(clientSocketChannel.hashCode());
            if(historyMessage == null) {
                completeMessage = message;
            } else {
                completeMessage = historyMessage.append(message).toString();
            }
			
			System.out.println("读取到客户端信息：" + completeMessage);
			
			ByteBuffer sendBuffer = ByteBuffer.wrap(URLEncoder.encode("来自服务器的信息", "UTF-8").getBytes());
			clientSocketChannel.write(sendBuffer);
			clientSocketChannel.close();
		}else{
			System.out.println("数据接收到一半：" + message);
			buffer.position(realLen);
			buffer.limit(buffer.capacity());
			
			//每一个channel对象都是独立的，所以可以使用对象的hash值，作为唯一标示
            Integer channelUUID = clientSocketChannel.hashCode();

            //然后获取这个channel下以前已经达到的message信息
            StringBuffer historyMessage = textMap.get(channelUUID);
            if(historyMessage == null) {
                historyMessage = new StringBuffer();
                textMap.put(channelUUID, historyMessage.append(message));
            }
		}
		
	}
}
