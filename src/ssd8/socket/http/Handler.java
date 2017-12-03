package ssd8.socket.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author Hanxy
 * @version 1.0.2
 */
public class Handler implements Runnable {

	private Socket socket;

	BufferedInputStream istream = null;
	BufferedOutputStream ostream = null;
	private StringBuffer header = null;
	private byte[] buffer;

	static private String CRLF = "\r\n";
	static private String pathname = "D:\\www";
	private static int buffer_size = 8192;

	public Handler( Socket socket) {
		this.socket = socket;
		buffer = new byte[buffer_size];
		header = new StringBuffer();
	}

	// 初始化输入输出流对象方法
	public void initStream() throws IOException {
		istream = new BufferedInputStream(socket.getInputStream());
		ostream = new BufferedOutputStream(socket.getOutputStream());
	}

	@Override
	public void run() {
		try {
			initStream();

			// 读取请求的头部
			int last = 0, c = 0;
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
					// 获得一行命令
					headers.add(header.toString());
					// 清空
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
            File saveFile = null;
            long fileSize = 0;
			String dirname = "";
			// 读取header内容
			for (String info : headers) {
			    //执行GET方法
				if (info.startsWith("GET")) {
					isFormat = false;
					doGet(info);
					break;
				} else if (info.startsWith("PUT")) {
					String[] reqs = info.split(" ");
					if (reqs.length != 3) {
						isFormat = false;
						String response = "HTTP/1.1 400 Bad Request" + CRLF
								+ CRLF;
						buffer = response.getBytes();
						ostream.write(buffer, 0, response.length());
						ostream.flush();
						break;
					} else {
						// 提取文件路径
						dirname = reqs[1];
						saveFile = new File(pathname + dirname);
					}
				} else if (info.startsWith("Content-length")) {
					// 提取内容长度
					fileSize = Long.parseLong(info.split(" ")[1].trim(), 10);
				}
			}

			if (isFormat) {
				// 内容长度为0，无需读取body
				if (fileSize == 0) {
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
						if (createFile(pathname + dirname)) {
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
						if (!createFile(pathname + dirname)) {
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
							if (recInfo.size() == fileSize) {
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
						String fileType = getFileType(dirname);

						if (isFile) {
							// 已有文件的修改
							String response = "HTTP/1.1 200 OK" + CRLF;
							response += "Server: " + "Hanxy/1.0" + CRLF;
							response += "Content-type: " + fileType + CRLF;
							response += "Content-Location: " + dirname + CRLF
									+ CRLF;
							buffer = response.getBytes();
							ostream.write(buffer, 0, response.length());
							ostream.flush();
						} else {
							// 新文件的创建
							String response = "HTTP/1.1 201 Created" + CRLF;
							response += "Server: " + "Hanxy/1.0" + CRLF;
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
		} finally {
			if (null != socket) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

    /**
     * 根据GET命令信息执行GET方法
     *
     * @param info GET命令信息
     * @return GET是否正确执行
     * @throws IOException 输出流Exception
     */
    private boolean doGet(String info) throws IOException {
	    String dirname = "";
	    long fileSize = 0;
	    File saveFile = null;

	    String[] reqs = info.split(" ");
	    if (reqs.length != 3) {
	        String response = "HTTP/1.1 400 Bad Request" + CRLF + CRLF;
	        buffer = response.getBytes();
	        ostream.write(buffer, 0, response.length());
	        ostream.flush();
	        return false;
        }else {
	        // 提取文件路径
            dirname = reqs[1];
            saveFile = new File(pathname + dirname);
            if (!saveFile.exists()){
                //没有该文件
                String response = "HTTP/1.1 404 NOT FOUND" + CRLF + CRLF;
                buffer = response.getBytes();
                ostream.write(buffer, 0, response.length());
                return false;
            }else {
                String fileType = getFileType(dirname);
                fileSize = saveFile.length();

                String response = "HTTP/1.1 200 OK" + CRLF;
                response += "Server: Hanxy/1.0" + CRLF;
                response += "Content-type: " + fileType + CRLF;
                response += "Content-length: " + fileSize + CRLF + CRLF;

                buffer = response.getBytes();
                ostream.write(buffer, 0, response.length());
                try {
                    getFileBody(saveFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ostream.flush();
            }
        }
	    return true;
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
	    buffer = new byte[buffer_size];
		BufferedInputStream bufferedInputStream = new BufferedInputStream(
				new FileInputStream(saveFile));
		while ((bufferedInputStream.read(buffer)) > 0) {
			ostream.write(buffer, 0, buffer.length);
			buffer = new byte[buffer_size];
		}
		bufferedInputStream.close();
	}

    /**
     * 根据文件路径获取文件类型
     *
     * @param dirname 文件路径
     * @return 文件类型
     */
    private String getFileType(String dirname){
	    String fileType = "Unknown Type";
        String[] path = dirname.split("/");
        if (path[path.length - 1].contains("jpg")) {
            fileType = "image/jpeg";
        } else if (path[path.length - 1].contains("htm")) {
            fileType = "text/html";
        } else if (path[path.length - 1].contains("txt")) {
            fileType = "text/plain";
        }
	    return fileType;
    }
}
