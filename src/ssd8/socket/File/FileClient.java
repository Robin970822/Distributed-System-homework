package ssd8.socket.File;

import ssd8.socket.tcp.echo.EchoClient;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class FileClient {
    private static final int TCP_PORT = 2021; //连接端口
    private static final String HOST = "127.0.0.1"; //连接地址
    Socket socket = new Socket();

    private static final int UDP_PORT = 2022; //UDP端口
    private static final int SENDSIZE = 512; //一次传送文件的字节数
    DatagramSocket datagramSocket;

    public FileClient() throws UnknownHostException, IOException{
        socket = new Socket(HOST, TCP_PORT); //创建客户端套接字
    }

    public static void main(String[] args) throws UnknownHostException, IOException{
        new EchoClient().send();
    }

}
