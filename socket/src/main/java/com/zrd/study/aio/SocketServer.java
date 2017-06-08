package com.zrd.study.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
	private static final Object waitObject = new Object();
	 
	public static void main(String[] args) throws IOException, InterruptedException {
		ExecutorService threadPool = Executors.newFixedThreadPool(20);
		AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(threadPool);
		final AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open(group);
		
		serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", 84));
		System.out.println("服务器启动...");
		serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>(){

			@Override
			public void completed(AsynchronousSocketChannel result, Void attachment) {
				System.out.println("接受到信息");
				
				serverSocketChannel.accept(attachment, this);
				
				ByteBuffer readBuffer = ByteBuffer.allocate(50);
				result.read(readBuffer, new StringBuffer(), new CompletionHandler<Integer, StringBuffer>(){

					@Override
					public void completed(Integer result1, StringBuffer attachment) {
						//如果条件成立，说明客户端主动终止了TCP套接字，这时服务端终止就可以了
				        if(result1 == -1) {
				            try {
				            	result.close();
				            } catch (IOException e) {
				                e.printStackTrace();
				            }
				            return;
				        }

				        System.out.println("completed(Integer result, Void attachment) : 然后我们来取出通道中准备好的值");
				        /*
				         * 实际上，由于我们从Integer result知道了本次channel从操作系统获取数据总长度
				         * 所以实际上，我们不需要切换成“读模式”的，但是为了保证编码的规范性，还是建议进行切换。
				         * 
				         * 另外，无论是JAVA AIO框架还是JAVA NIO框架，都会出现“buffer的总容量”小于“当前从操作系统获取到的总数据量”，
				         * 但区别是，JAVA AIO框架中，我们不需要专门考虑处理这样的情况，因为JAVA AIO框架已经帮我们做了处理（做成了多次通知）
				         * */
				        readBuffer.flip();
				        byte[] contexts = new byte[1024];
				        readBuffer.get(contexts, 0, result1);
				        readBuffer.clear();
				        try {
				            String nowContent = new String(contexts , 0 , result1 , "UTF-8");
				            attachment.append(nowContent);
				            System.out.println("================目前的传输结果：" + attachment);
				        } catch (UnsupportedEncodingException e) {
				            e.printStackTrace();
				        }
				        
				        //如果条件成立，说明还没有接收到“结束标记”
				        if(attachment.indexOf("\\r\\n") == -1) {
				        	//还要继续监听（一次监听一次通知）
					        result.read(readBuffer, attachment, this);
				            return;
				        }
				        
				        //=========================================================================
				        //          和上篇文章的代码相同，我们以“over”符号作为客户端完整信息的标记
				        //=========================================================================
				        System.out.println("=======收到完整信息，开始处理业务=========：" + attachment);
				        attachment = new StringBuffer();

					}

					@Override
					public void failed(Throwable exc, StringBuffer attachment) {
						// TODO Auto-generated method stub
						
					}
					
				});
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		synchronized(waitObject) {
            waitObject.wait();
        }
	}
}
