package ssd8.socket.ex01;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Robin Hanxy
 * @version 1.0.2
 */
public class FileServer {
    ServerSocket serverSocket;
    private final int tcp_PORT = 2021; //TCP端口
    ExecutorService executorService; // 线程池
    final int POOLSIZE = 10; // 单个处理器线程池同时工作线程数目

    public FileServer() throws IOException {
        serverSocket = new ServerSocket(tcp_PORT); // 创建服务器端套接字
        // 创建线程池
        // Runtime的availableProcessors()方法返回当前系统可用处理器的数目
        // 由JVM根据系统的情况来决定线程的数量
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors() * POOLSIZE);
        System.out.println("服务器启动");
    }

    public static void main(String[] args) throws IOException {
        new FileServer().service(); // 启动服务
    }

    /**
     * service implements
     */
    public void service() {
        Socket socket = null;
        while (true) {
            try {
                socket = serverSocket.accept(); // 等待用户连接
                executorService.execute(new Handler(socket)); // 把执行交给线程池来维护
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

