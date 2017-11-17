package ssd8.socket.File;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class FileClient {
    private static final int TCP_PORT = 2021; //连接端口
    private static final String HOST = "127.0.0.1"; //连接地址
    Socket socket = new Socket();

    private static final int UDP_PORT = 2022; //UDP端口
    private static final int SENDSIZE = 512; //一次传送文件的字节数
    DatagramSocket datagramSocket;

    public FileClient() throws UnknownHostException, IOException {
        socket = new Socket(HOST, TCP_PORT); //创建客户端套接字
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        new FileClient().send();
    }

    /**
     *
     */
    private void send() {
        try {
            //客户端输出流，向服务器发消息
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //客户端输入流，接收服务器消息
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //装饰输出流，及时刷新
            PrintWriter pw = new PrintWriter(bw, true);
            //接受用户信息
            Scanner in = new Scanner(System.in);

            String cmd = null;
            while ((cmd = in.next()) != null) {
                pw.println(cmd);//发送给服务器
                if (cmd.equals("cd") || cmd.equals("get")) {
                    String dir = in.next();
                    pw.println(dir);
                    if (cmd.equals("get")) {//下载文件
//                        long fileLength = Long.parseLong(br.readLine());
                        long fileLength = 2024;
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
                    if (msg.equals("End")) {
                        break;
                    }
                    System.out.println(msg);
                }

                if (cmd.equals("bye")) {
                    break;
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
                    socket.close();//断开连接
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param dir
     * @param fileLength
     * @throws IOException
     */
    private void getFile(String dir, long fileLength) throws IOException {
        DatagramPacket dp = new DatagramPacket(new byte[SENDSIZE], SENDSIZE);
        datagramSocket = new DatagramSocket(UDP_PORT);// UDP连接
        String msg = null;
        byte[] recInfo = new byte[SENDSIZE];
        FileOutputStream fos = new FileOutputStream(new File(
                ("D:\\Download\\") + dir));

        int count = (int) (fileLength / SENDSIZE) + ((fileLength % SENDSIZE) == 0 ? 0 : 1);

        while ((count--) > 0) {
            datagramSocket.receive(dp); // 接收文件信息
            recInfo = dp.getData();
            fos.write(recInfo, 0, dp.getLength());
            fos.flush();
        }

        datagramSocket.close();
        fos.close();
    }
}
