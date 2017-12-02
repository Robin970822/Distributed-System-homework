package ssd8.socket.http;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author Robin Hanxy
 * @version 1.0.0
 */
public class Handler implements Runnable {

    /**
     * default HTTP port is port 80
     */
    private static int port = 80;

    /**
     * Allow a maximum buffer size of 8192 bytes
     */
    private static int buffer_size = 8192;

    /**
     * Response is stored in a byte array.
     */
    private byte[] buffer;

    /**
     * My socket to the world.
     */
    private Socket socket = null;

    /**
     * Default port is 80.
     */
    private static final int PORT = 80;

    /**
     * Output stream to the socket.
     */
    BufferedOutputStream ostream = null;

    /**
     * Input stream from the socket.
     */
    BufferedInputStream istream = null;

    /**
     * StringBuffer storing the header
     */
    private StringBuffer header = null;

    /**
     * StringBuffer storing the response.
     */
    private StringBuffer response = null;

    /**
     * String to represent the Carriage Return and Line Feed character sequence.
     */
    static private String CRLF = "\r\n";

    /**
     * root path
     */
    static private String root = "D:\\www";


    public Handler(Socket socket) {
        this.socket = socket;
        buffer = new byte[buffer_size];
        header = new StringBuffer();
        response = new StringBuffer();
    }

    /**
     * <em>initStream</em> initials input stream and output stream object.
     *
     * @throws IOException
     */
    public void initStream() throws IOException {
        istream = new BufferedInputStream(socket.getInputStream());
        ostream = new BufferedOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            initStream();
            System.out.println("Hello!");

            File saveFile = null;
            long fileLength = 0;

            int last = 0, c = 0;
            //Process the header and add it to the header StringBuffer
            boolean isHeader = true;
            ArrayList<String> headers = new ArrayList<String>();
            while (isHeader && ((c = istream.read()) != -1)) {
                switch (c) {
                    case '\r':
                        break;
                    case '\n':
                        if (c == last) {
                            isHeader = false;
                            break;
                        }
                        last = c;
                        header.append('\n');
                        //获得一行命令
                        headers.add(header.toString());
                        //清空
                        header = new StringBuffer();
                        break;
                    default:
                        last = c;
                        header.append((char) c);
                        break;
                }
            }

            // 是否为PUT正确的格式
            boolean isFormat = true;
            String dirname = "";
            //读取header内容
            for (String info : headers){
                if (info.startsWith("GET")){
                    isFormat = false;
                    String[] reqs = info.split(" ");
                    if (reqs.length != 3) {
                        String response = "HTTP/1.0 400 Bad Request" + CRLF + CRLF;
                        buffer = response.getBytes();
                        ostream.write(buffer, 0, response.length());
                        ostream.flush();
                        break;
                    }else {
                        // 提取文件路径
                        dirname = reqs[1];
                        saveFile = new File(root + dirname);
                        if (!saveFile.exists()) {
                            String response = "HTTP/1.0 400 Not Found" + CRLF + CRLF;
                            buffer = response.getBytes();
                            ostream.write(buffer, 0, response.length());
                            ostream.flush();
                            break;
                        }else {
                            String fileType = "Unknown Type";
                            String[] path = dirname.split("/");
                            if (path[path.length - 1 ].contains("jpg")) {
                                fileType = "image/jpeg";
                            }else if (path[path.length - 1].contains("htm")){
                                fileType = "text/html";
                            }else if (path[path.length - 1].contains("txt")){
                                fileType = "text/plain";
                            }

                            fileLength = saveFile.length();

                            String response = "HTTP/1.1 200 OK" + CRLF;
                            response += "Server: Hanxy/1.0" + CRLF;
                            response += "Content-type: " + fileType + CRLF;
                            response += "Content-length: " + fileLength + CRLF + CRLF;

                            buffer = response.getBytes();
                            ostream.write(buffer, 0, response.length());

                            try{
                                getFileBody(saveFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            ostream.flush();
                        }
                    }
                }else if (info.startsWith("PUT")) {
                    String[] reqs = info.split(" ");
                    if (reqs.length != 3){
                        isFormat = false;
                        String response = "HTTP/1.1 400 Bad Request" + CRLF
                                + CRLF;
                        buffer = response.getBytes();
                        ostream.write(buffer, 0, response.length());
                        ostream.flush();
                        break;
                    }else {
                        // 提取文件路径
                        dirname = reqs[1];
                        saveFile = new File(root + dirname);
                    }
                }else if (info.startsWith("Content-length")) {
                    // 提取内容长度
                    fileLength = Long.parseLong(info.split(" ")[1].trim(), 10);
                }
            }

            if (isFormat) {
                // 内容长度为0，无需读取body
                if (fileLength == 0) {
                    if (saveFile.exists()) {
                        // 清空文件
                        FileWriter fileWriter = new FileWriter(saveFile);
                        fileWriter.write("");
                        fileWriter.close();
                        String response = "HTTP/1.1 204 No Content" + CRLF;
                        response += "Content-Location: " + dirname + CRLF
                                + CRLF;
                        buffer = response.getBytes();
                        ostream.write(buffer, 0, response.length());
                        ostream.flush();
                    } else {
                        if (createFile(root + dirname)) {
                            String response = "HTTP/1.1 201 Created" + CRLF;
                            response += "Content-Location: " + dirname + CRLF
                                    + CRLF;
                            buffer = response.getBytes();
                            ostream.write(buffer, 0, response.length());
                            ostream.flush();
                        } else {
                            String response = "HTTP/1.1 500 Internal Server Error"
                                    + CRLF + CRLF;
                            buffer = response.getBytes();
                            ostream.write(buffer, 0, response.length());
                            ostream.flush();
                        }
                    }
                } else {
                    boolean isFile = saveFile.exists();
                    if (!isFile) {
                        if (!createFile(root + dirname)) {
                            String response = "HTTP/1.1 500 Internal Server Error"
                                    + CRLF + CRLF;
                            buffer = response.getBytes();
                            ostream.write(buffer, 0, response.length());
                            ostream.flush();
                        }
                    }

                    if (saveFile.exists()) {
                        ArrayList<Byte> recInfo = new ArrayList<Byte>();
                        int b = 0;
                        while ((b = istream.read()) != -1) {
                            recInfo.add(new Byte((byte) b));
                            // 读取到指定大小以结束读取
                            if (recInfo.size() == fileLength) {
                                break;
                            }
                        }

                        // 转化成数组
                        byte[] recBytes = new byte[recInfo.size()];
                        for (int i = 0; i < recInfo.size(); i++) {
                            recBytes[i] = recInfo.get(i).byteValue();
                        }

                        // 写入到文件
                        BufferedOutputStream outputStream = new BufferedOutputStream(
                                new FileOutputStream(saveFile));
                        outputStream.write(recBytes);
                        outputStream.flush();
                        outputStream.close();

                        // 判断文件类型
                        String fileType = "Unknown Type";
                        String[] path = dirname.split("/");
                        if (path[path.length - 1].contains("jpg")) {
                            fileType = "image/jpeg";
                        } else if (path[path.length - 1].contains("htm")) {
                            fileType = "text/html";
                        } else if (path[path.length - 1].contains("txt")) {
                            fileType = "text/plain";
                        }

                        if (isFile) {
                            // 已有文件的修改
                            String response = "HTTP/1.1 200 OK" + CRLF;
                            response += "Server: " + "GuYuhong/1.0" + CRLF;
                            response += "Content-type: " + fileType + CRLF;
                            response += "Content-Location: " + dirname + CRLF
                                    + CRLF;
                            buffer = response.getBytes();
                            ostream.write(buffer, 0, response.length());
                            ostream.flush();
                        } else {
                            // 新文件的创建
                            String response = "HTTP/1.1 201 Created" + CRLF;
                            response += "Server: " + "GuYuhong/1.0" + CRLF;
                            response += "Content-type: " + fileType + CRLF;
                            response += "Content-Location: " + dirname + CRLF
                                    + CRLF;
                            buffer = response.getBytes();
                            ostream.write(buffer, 0, response.length());
                            ostream.flush();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null != socket) {
                try {
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建文件
     *
     * @param filename
     *            文件名
     * @return boolean 是否创建成功
     */
    private boolean createFile(String filename) {
        File file = new File(filename);
        if (filename.endsWith(File.separator)) {
            System.out.println("不能为目录");
            return false;
        }
        if (!file.getParentFile().exists()) {
            System.out.println("目标文件所在目录不存在，开始创建它!");
            if (!file.getParentFile().mkdirs()) {
                System.out.println("创建目标文件所在目录失败！");
                return false;
            }
        }

        try {
            if (file.createNewFile()) {
                System.out.println("创建文件成功");
                return true;
            } else {
                System.out.println("创建文件失败");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("出错，创建文件失败");
            return false;
        }
    }

    /**
     * 得到文件的内容
     *
     * @param saveFile
     *            需要服务器保存的文件
     * @throws Exception
     */
    private void getFileBody(File saveFile) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new FileInputStream(saveFile));
        while ((bufferedInputStream.read(buffer)) > 0) {
            ostream.write(buffer, 0, buffer.length);
            buffer = new byte[buffer_size];
        }
        bufferedInputStream.close();
    }
}
