package ssd8.socket.File;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FileServer
 *
 * @author hanxy
 */
public class FileServer {
    ServerSocket serverSocket;
    private final int port = 2021;
    private final String root = "D://";

    /**
     * @throws IOException
     */
    public FileServer() throws IOException {
        //创建服务器端套接字
        serverSocket = new ServerSocket(port, 2);
        System.out.print("服务器启动");
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new FileServer().service();
    }

    /**
     * service implements
     */
    public void service() {
        Socket socket = null;
        while (true) {
            try {
                //等待并取出用户连接，并创捷套接字
                socket = serverSocket.accept();
                //客户端信息
                System.out.println("新连接，连接地址：" + socket.getInetAddress()
                        + "：" + socket.getPort());
                //输入流，读取客户信息
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                //输出流，向客户端写信息
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream()));
                //装饰输出流，true,每写一行就刷新输出缓冲区，不用flush
                PrintWriter pw = new PrintWriter(bw, true);

                String info = null;
                String currentPath = root;
                File currentFile = new File(currentPath);

                while ((info = br.readLine()) != null){
                    if (info.equals("ls")){
                        pw.println(getFileInfoList(currentFile));
                    }else if (info.startsWith("cd")){
                    }else if (info.startsWith("get")){
                    }else if (info.equals("bye")){
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (null != socket){
                    try {
                        socket.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @param filepath
     * @return
     */
    private String getFileInfoList(String filepath){
        String FileInfoList = "";
        return FileInfoList;
    }

    /**
     * @param file
     * @return
     */
    private String getFileInfoList(File file){
        String FileInfoList = "";

        File[] files = file.listFiles();

        for (File temp:files) {
            if (temp.isFile()){
                FileInfoList += "<file>" + "\t";
                FileInfoList += temp.getName() + "\t";
                FileInfoList += temp.length() + "\n";
            }else if (temp.isDirectory()){
                FileInfoList += "<dir>" + "\t";
                FileInfoList += temp.getName() + "\t";
                FileInfoList += temp.length() + "\n";
            }
        }

        return FileInfoList;
    }

}
