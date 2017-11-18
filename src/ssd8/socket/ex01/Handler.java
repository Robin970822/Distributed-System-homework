package ssd8.socket.ex01;

import java.io.*;
import java.net.*;

/**
 * @author Robin Hanxy
 * @version 1.0.2
 *
 * implements {@link Runnable}
 */
public class Handler implements Runnable { // 负责与单个客户通信的线程

    private Socket socket;
    BufferedReader br;
    BufferedWriter bw;
    PrintWriter pw;
    private final String rootPath = "D:\\Coffee";
    public static String currentPath = "D:\\Coffee";

    private static final String HOST = "127.0.0.1"; // 连接地址
    private static final int UDP_PORT = 2020; // UDP端口
    private static final int SENDSIZE = 1024; // 一次传送文件的字节数
    DatagramSocket dgsocket; // UDP用于传送文件
    SocketAddress socketAddres;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public void initStream() throws IOException { // 初始化输入输出流对象方法
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bw = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));
        pw = new PrintWriter(bw, true);
    }

    public void run() { // 执行的内容
        try {
            System.out.println("新连接，连接地址：" + socket.getInetAddress() + "："
                    + socket.getPort()); // 客户端信息

            initStream(); // 初始化输入输出流对象

            pw.println(socket.getInetAddress() + ":" + socket.getPort()
                    + ">连接成功");// 向客户端发送连接成功信息

            String info = null;

            while (null != (info = br.readLine())) {
                if (info.equals("bye")) {// 退出
                    break;
                } else {
                    switch (info) {
                        case "ls":
                            ls(currentPath);
                            break;
                        case "cd":
                            String dir = null;
                            if (null != (dir = br.readLine())) {
                                cd(dir);
                            } else {
                                pw.println("please input a direction after cd");
                            }
                            break;
                        case "cd..":
                            backDir();
                            break;
                        case "get":
                            String fileName = br.readLine();
                            sendFile(fileName);
                            break;
                        default:
                            pw.println("unknown cmd");
                    }
                    pw.println("CmdEnd");// 用于标识目前的指令结束，以帮助跳出Client的输出循环
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                try {
                    br.close();
                    bw.close();
                    pw.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Sends a file by fileName
     *
     * @param fileName the fileName of the file that wants sending
     * @throws SocketException
     * @throws IOException
     * @throws InterruptedException
     */
    private void sendFile(String fileName) throws SocketException, IOException,
            InterruptedException {

        if (!isFileExist(fileName)) {
            pw.println(-1);
            return;// 文件不存在
        }

        File file = new File(currentPath + "\\" + fileName);
        pw.println(file.length());

        dgsocket = new DatagramSocket(); // UDP
        socketAddres = new InetSocketAddress(HOST, UDP_PORT);
        DatagramPacket dp;

        byte[] sendInfo = new byte[SENDSIZE];
        int size = 0;
        dp = new DatagramPacket(sendInfo, sendInfo.length, socketAddres);
        BufferedInputStream bfdIS = new BufferedInputStream(
                new FileInputStream(file));

        while ((size = bfdIS.read(sendInfo)) > 0) {
            dp.setData(sendInfo);
            dgsocket.send(dp);
            sendInfo = new byte[SENDSIZE];
        }

        dgsocket.close();
    }
    
    /**
     * Implement cd.. function
     * Back to upper direction
     */
    private void backDir() {
        if (currentPath.equals(rootPath)) {
            pw.println("The current path is the root path, cd.. cannot realize.");
        } else {
            for (int i = currentPath.length(); i > 0; i--) {
                if (currentPath.substring(i - 1, i).equals("\\")) {
                    currentPath = currentPath.substring(0, i - 1);
                    pw.println((new File(currentPath)).getName() + " > OK");
                    break;
                }
            }
        }
    }

    /**
     * Lists files and directions in the current path
     *
     * @param currentPath the path wants listing
     */
    private void ls(String currentPath) {
        File rootFile = new File(currentPath);
        File[] fileList = rootFile.listFiles();
        int MaxLength = 40;
        pw.println("Type" + "\t" + "Name" + addSpace(MaxLength - 4) + "Size");
        for (File file : fileList) {
            if (file.isFile()) {// 是文件
                pw.println("<file>" + "\t" + file.getName()
                        + addSpace(MaxLength - file.getName().length())
                        + file.length() + "B");
            } else if (file.isDirectory()) {// 是文件夹
                pw.println("<dir>" + "\t" + file.getName()
                        + addSpace(MaxLength - file.getName().length())
                        + file.length() + "B");
            }
        }
    }

    /**
     * Aligns prints by adding space
     *
     * @param count the count of space needs printing
     * @return the space
     */
    public static String addSpace(int count) {
        String str = "";
        for (int i = 0; i < count; i++) {
            str += " ";
        }
        return str;
    }

    /**
     * Changes direction into dir
     *
     * @param dir the direction wants changing into
     */
    private void cd(String dir) {
        Boolean isExist = false;// 初始设定目录不存在
        Boolean isDir = true;// 初始设定是文件夹
        File rootFile = new File(currentPath);
        File[] fileList = rootFile.listFiles();
        for (File file : fileList) {
            if (file.getName().equals(dir)) {// 找到了同名的文件夹或文件
                isExist = true;
                if (file.isDirectory()) {// 名字对应文件夹
                    isDir = true;
                    break;
                } else {// 名字对应文件
                    isDir = false;
                    pw.println("You cannot cd file, only direction admitted");
                }
            }
        }
        if (isExist && isDir) {// 是文件夹并且存在
            currentPath = currentPath + "\\" + dir;
            pw.println(dir + " > OK");
        } else if (isDir && (!isExist)) {// 是文件夹但不在当前目录
            pw.println(dir + " direction not exist!");
        }

    }

    /**
     * Judges if file exists in current path
     *
     * @param fileName the name of file needs judging
     * @return if file exits in current path
     */
    public static boolean isFileExist(String fileName) {
        boolean isExist = false;//假设文件不存在于当前目录

        File rootFile = new File(currentPath);
        File[] fileList = rootFile.listFiles();

        for (File file:fileList){
            if (file.getName().equals(fileName) && file.isFile()){
                isExist = true;
            }
        }
        return isExist;
    }
}

