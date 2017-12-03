package ssd8.socket.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Hanxy
 * @version 1.0.1
 */
public class Server {
	ServerSocket serverSocket;

	private final int PORT = 80;
	ExecutorService executorService;
	final int POOLSIZE = 4;

	public Server() throws IOException {
		serverSocket = new ServerSocket(PORT);
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().
					availableProcessors() * POOLSIZE);
		System.out.println("服务器启动，监听80端口");
	}

	public static void main(String[] args) throws IOException {
		new Server().servic();
	}

	public void servic() {
		Socket socket = null;
		while (true) {
			try {
				socket = serverSocket.accept();
				executorService.execute(new Handler(socket));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
