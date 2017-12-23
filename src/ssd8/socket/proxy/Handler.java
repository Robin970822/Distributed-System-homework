package ssd8.socket.proxy;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;

/**
 * @author Hanxy
 * @version 1.0.3
 * <p>
 * implements {@link Runnable}
 */
public class Handler implements Runnable {// 负责与单个客户通信的线程

    private Socket socket;
    private ProxyClient proxyClient;

    private byte[] buffer;
    private static int buffer_size = 8192;//一次传送文件的字节数
    private static String CRLF = "\r\n";

    // 代理服务器作为服务端的输入输出流
    private BufferedReader br = null;
    private BufferedOutputStream ostream = null;

    /**
     * 初始化输入输出流对象方法
     *
     * @throws IOException
     */
    public void initStream() throws IOException {
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ostream = new BufferedOutputStream(socket.getOutputStream());
    }

    public Handler(Socket socket) {
        this.socket = socket;
        //每次刷新缓存，防止重复
        buffer = new byte[buffer_size];
        this.proxyClient = new ProxyClient();
    }

    @Override
    public void run() {//执行内容
        try {
            initStream();
            String info = null;

            //获取请求信息
            while ((info = br.readLine()) != null) {
                if (info.toLowerCase().startsWith("get")) {//取得GET请求
                    doGet(info);
                } else if (info.toLowerCase().startsWith("from") || info.toLowerCase()
                        .startsWith("user-agent")) {
                } else {
                    badRequest();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Return Code 400 Bad Request
     *
     * @throws IOException
     */
    private void badRequest() throws IOException {
        String response = "HTTP/1.0 400 Bad Request" + CRLF + CRLF;
        buffer = response.getBytes();
        ostream.write(buffer, 0, response.length());
        ostream.flush();
    }

    /**
     * 根据请求信息实现GET方法
     *
     * @param info 请求信息
     * @return 是否正确执行GET方法
     * @throws Exception
     */
    private boolean doGet(String info) throws Exception {
        URL url = null;

        String[] reqs = info.split(" ");//分割请求
        if (!(reqs.length == 3)) {
            badRequest();
            return false;
        } else {
            url = new URL(reqs[1]);
            requestGet(url);
            responseGet();
        }
        return true;
    }


    /**
     * 根据url发送GET请求
     *
     * @param url url
     * @throws Exception
     */
    private void requestGet(URL url) throws Exception {
        // 默认去连接80端口
        proxyClient.connect(url.getHost(),
                url.getPort() == -1 ? 80 : url.getPort());
        String request = "GET " + url.getFile() + " HTTP/1.1";
        proxyClient.processGetRequest(request, url.getHost());
    }

    /**
     * 得到GET回复，返回给客户端
     *
     * @throws IOException
     */
    private void responseGet() throws IOException {
        String header = proxyClient.getHeader() + "\n";
        String body = proxyClient.getResponse();

        buffer = header.getBytes();
        ostream.write(buffer, 0, header.length());
        ostream.write(body.getBytes());

        ostream.flush();
    }

}

