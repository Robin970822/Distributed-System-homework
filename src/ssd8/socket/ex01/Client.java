package ssd8.socket.ex01;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Handler;

public class Client {
	private static final int tcp_PORT = 2021; // TCP连接端口
	private static final String HOST = "127.0.0.1"; // 连接地址
	Socket socket = new Socket();

	private static final int udp_PORT = 2020; // UDP端口
	private static final int SENDSIZE = 512; // 一次传送文件的字节数
	DatagramSocket dgsocket;

	public Client() throws UnknownHostException, IOException {
		 socket = new Socket(HOST, tcp_PORT); //创建客户端套接字

	}

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		new Client().send();
	}

	/**
	 * send implements
	 */
	public void send() {
		try {
			// 客户端输出流，向服务器发消息
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			// 客户端输入流，接收服务器消息
			BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			System.out.println(br.readLine()); // 输出服务器返回连接成功的消息

			PrintWriter pw = new PrintWriter(bw, true); 
			Scanner in = new Scanner(System.in); // 接受用户信息
			String cmd = null;
			while ((cmd = in.next()) != null) {
				pw.println(cmd); // 发送给服务器端
				if (cmd.equals("cd") || cmd.equals("get")) {
					String dir = in.next();
					pw.println(dir);
					if (cmd.equals("get")) {// 下载文件
						String currentP = br.readLine();
						if (fileExist(currentP, dir)) {
							System.out.println("开始接收文件：" + dir);	
							long fileLength = (new File(currentP+"\\" + dir)).length();
							getFile(dir, fileLength);
							System.out.println("文件接收完毕");
						} else {
							System.out.println( "unknown file");
						}
					}
				}

				String msg = null;
				while (null != (msg = br.readLine())) {
					if (msg.equals("CmdEnd")) {
						break;
					}
					System.out.println(msg); // 输出服务器返回的消息
				}

				if (cmd.equals("bye")) {
					break; // 退出
				}

			}
			in.close();
			br.close();
			bw.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != socket) {
				try {
					socket.close(); // 断开连接
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void getFile(String dir, long fileLength) throws IOException {
		DatagramPacket dp = new DatagramPacket(new byte[SENDSIZE], SENDSIZE);
		dgsocket = new DatagramSocket(udp_PORT);// UDP连接
		String msg = null;
		byte[] recInfo = new byte[SENDSIZE];
		FileOutputStream fos = new FileOutputStream(new File(
				("D:\\Download\\") + dir));

		int count = (int)(fileLength / SENDSIZE) + ((fileLength % SENDSIZE) == 0 ? 0 : 1) ;
		
		while ((count--)>0) {
			dgsocket.receive(dp); // 接收文件信息
			recInfo = dp.getData();
			fos.write(recInfo, 0, dp.getLength());
			fos.flush();
		}

		dgsocket.close();
		fos.close();
	}

	public static boolean fileExist(String currentPath, String fileName) {
		boolean isExist = false;
		File rootFile = new File(currentPath);
		File[] fileList = rootFile.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].getName().equals(fileName) && fileList[i].isFile()) {// 找到了同名的文件夹或文件
				isExist = true;
			}
		}
		return isExist;
	}
}

