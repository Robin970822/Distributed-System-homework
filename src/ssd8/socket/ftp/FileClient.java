package ssd8.socket.ftp;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * @author Robin Hanxy
 * @version 1.0.5
 */
public class FileClient {
    private static final int tcp_PORT = 2021; // TCP连接端口
    private static final String HOST = "127.0.0.1"; // 连接地址
    private static final int udp_PORT = 2020; // UDP端口
    private static final int SENDSIZE = 1024; // 一次传送文件的字节数
    Socket socket = new Socket();
    DatagramSocket dgsocket;

    public FileClient() throws UnknownHostException, IOException {
        socket = new Socket(HOST, tcp_PORT); //创建客户端套接字

    }

    public static void main(String[] args) throws UnknownHostException,
            IOException {
        new FileClient().send();
    }

    /**
     * send implements
     */
    public void send() {
        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream()));// 客户端输出流，向服务器发消息
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));// 客户端输入流，接收服务器消息
            PrintWriter pw = new PrintWriter(bw, true);//装饰输出流，及时刷新

            System.out.println(br.readLine()); // 输出服务器返回连接成功的消息

            Scanner in = new Scanner(System.in); // 接受用户信息
            String cmd = null;
            while ((cmd = in.next()) != null) {
                pw.println(cmd); // 发送给服务器端
                if (cmd.equals("cd") || cmd.equals("get")) {
                    String dir = in.next();
                    pw.println(dir);
                    if (cmd.equals("get")) {// 下载文件
                        long fileLength = Long.parseLong(br.readLine());
                        System.out.println("文件大小为：" + fileLength);
                        if (fileLength != -1) {
                            System.out.println("开始接收文件：" + dir);
                            getFile(dir, fileLength);
                            System.out.println("文件接收完毕");
                        } else {
                            System.out.println("Unknown file");
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

    /**
     * Gets a file by name and length
     *
     * @param fileName the name of the file that wants getting.
     * @param fileLength the length of the file that wants getting.
     * @throws IOException
     */
    private void getFile(String fileName, long fileLength) throws IOException {
        DatagramPacket dp = new DatagramPacket(new byte[SENDSIZE], SENDSIZE);
        dgsocket = new DatagramSocket(udp_PORT);// UDP连接
        byte[] recInfo = new byte[SENDSIZE];
        FileOutputStream fos = new FileOutputStream(new File(
                ("D:\\Download\\") + fileName));

        int count = (int) (fileLength / SENDSIZE) + ((fileLength % SENDSIZE) == 0 ? 0 : 1);

        while ((count--) > 0) {
            dgsocket.receive(dp); // 接收文件信息
            recInfo = dp.getData();
            fos.write(recInfo, 0, dp.getLength());
            fos.flush();
        }

        dgsocket.close();
        fos.close();
    }
}

