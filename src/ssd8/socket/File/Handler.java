package ssd8.socket.File;

import java.io.*;
import java.net.*;

public class Handler implements Runnable {//负责与单个客户通信的线程

    private Socket socket;
    BufferedReader br;
    BufferedWriter bw;
    PrintWriter pw;

    private final String root = "D:\\Coffee";

    private static final String HOST = "127.0.0.1";//连接地址
    private static final int UDP_PORT = 2020;//UDP端口
    private static final int SENDSIZE = 512;//一次传送文件的字节数
    DatagramSocket datagramSocket;//UDP用于传送文件
    SocketAddress socketAddress;


    public Handler(Socket socket) {
        this.socket = socket;
    }

    public void initStream() throws IOException {//初始化输入输出流对象方法
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        pw = new PrintWriter(bw, true);
    }

    @Override
    public void run() {
        try {

            System.out.println("新连接，连接地址：" + socket.getInetAddress()
                    + "：" + socket.getPort());// 客户端信息

            initStream();//初始化输入输出流对象

            String info = null;
            String currentPath = root;

            while (null != (info = br.readLine())) {
                if (info.equals("bye")) {//退出
                    break;
                } else {
                    switch (info) {
                        case "ls":
                            String fileInfoList = getFileInfoList(currentPath);
                            pw.println(fileInfoList);
                            break;
                        case "cd":
                            String dir = null;
                            if (null != (dir = br.readLine())) {
                                currentPath = cd(currentPath, dir);
                            } else {
                                pw.println("please input the direction after cd");
                            }
                            break;
                        case "cd..":
                            currentPath = backDir(currentPath);
                            pw.println(currentPath + "> OK");
                            break;
                        case "get":
                            String fileName = br.readLine();
                            getFile(currentPath, fileName);
                            break;
                        default:
                            pw.println("Unknown Command");
                    }
                    pw.println("End");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
     * @param filepath
     * @return
     */
    private String getFileInfoList(String filepath) {
        File currentFile = new File(filepath);
        String FileInfoList = "";

        if (null != currentFile) {
            FileInfoList = getFileInfoList(currentFile);
        } else {
            pw.println("Invalid file path !");
        }

        return FileInfoList;
    }

    /**
     * @param file
     * @return
     */
    private String getFileInfoList(File file) {
        String FileInfoList = "";

        File[] files = file.listFiles();

        for (File temp : files) {
            if (temp.isFile()) {
                FileInfoList += "<file>" + "\t";
                FileInfoList += temp.getName() + "\t";
                FileInfoList += temp.length() + "\n";
            } else if (temp.isDirectory()) {
                FileInfoList += "<dir>" + "\t";
                FileInfoList += temp.getName() + "\t";
                FileInfoList += temp.length() + "\n";
            }
        }

        return FileInfoList;
    }

    /**
     * @param currentPath
     * @param dir
     * @return
     */
    private String cd(String currentPath, String dir) {
        Boolean isExist = false;//初始设定目录不存在
        Boolean isDir = true;//初始设定是文件夹

        String newPath = currentPath;

        File currentFile = new File(currentPath);

        if (null != currentFile) {
            File[] files = currentFile.listFiles();

            for (File file : files) {
                if (file.getName().equals(dir)) {//找到同名文件获文件夹
                    isExist = true;
                    if (file.isDirectory()) {//对应文件夹
                        isDir = true;
                        newPath = file.getPath();
                        break;
                    } else {//对应文件
                        isDir = false;
                        pw.println("You cannot cd file, only direction admitted");
                    }
                }
            }
        } else isExist = false;
        if (isExist && isDir) {
            pw.println(dir + ">OK");
        } else if (isDir && (!isExist)) {
            pw.println(dir + " direction not exist!");
        }

        return newPath;
    }

    /**
     * @param currentPath
     * @return
     */
    private String backDir(String currentPath) {
        String parentPath = currentPath;

        if (currentPath.equals(root)) {
            pw.println("The current path is the root path.");
        } else {
            File currentFile = new File(currentPath);
            if (null != currentFile) {
                parentPath = backDir(currentFile);
            } else {
                pw.println("Invalid file path");
            }
        }
        return parentPath;
    }

    /**
     * @param file
     * @return
     */
    private String backDir(File file) {
        String parentPath = file.getParent();
        return parentPath;
    }

    private void getFile(String currentPath, String fileName) throws SocketException, IOException,
            InterruptedException {

//        pw.println(currentPath);

        if (!fileExist(currentPath,fileName)){
            pw.println(-1);
            return;
        }
        File file = new File(currentPath + "\\" + fileName);
        pw.println(file.length());

        datagramSocket = new DatagramSocket(); //UDP
        socketAddress = new InetSocketAddress(HOST, UDP_PORT);

        byte[] sendInfo = new byte[SENDSIZE];
        int size = 0;
        DatagramPacket datagramPacket = new DatagramPacket(sendInfo, sendInfo.length, socketAddress);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new FileInputStream(file));
        while ((size = bufferedInputStream.read(sendInfo)) > 0 ){
            datagramPacket.setData(sendInfo);
            datagramSocket.send(datagramPacket);
            sendInfo = new  byte[SENDSIZE];
        }
        datagramSocket.close();
    }

    private static boolean fileExist(String currentPath, String fileName) {
        boolean isExist = false;

        File currentFile = new File(currentPath);
        File[] files = currentFile.listFiles();

        for (File file : files) {
            if (file.getName().equals(fileName) && file.isFile()) {
                isExist = true;
            }
        }

        return isExist;
    }


}
