package ssd8.socket.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Robin Hanxy
 * @version 1.0.0
 */
public class Server {
    ServerSocket serverSocket;

    private final int PORT = 80;// http默认端口
    ExecutorService executorService;// 线程池
    final int POOLSIZE = 4;// 单个处理器线程池同时工作线程数目

    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);// 创建服务器端套接字
        // 创建线程池
        // Runtime的availableProcessors()方法返回当前系统可用处理器的数目
        // 由JVM根据系统的情况来决定线程的数量
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().
                availableProcessors() * POOLSIZE);
        System.out.println("服务器启动，监听80端口");
    }

    public static void main(String[] args) throws IOException {
        new Server().service();//启动服务
    }

    /**
     * service implements
     */
    public void service(){
        Socket socket = null;
        while (true){
            try{
                socket = serverSocket.accept();// 等待用户连接
                executorService.execute(new Handler(socket));// 把执行交给线程池来维护s
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
