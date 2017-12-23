package ssd8.rmi.client;

import java.rmi.Naming;

import ssd8.rmi.rface.HelloInterface;

public class HelloClient {
	public static void main(String[] argv) {
		try {
			HelloInterface hello = (HelloInterface) Naming.lookup("Hello");

			// 如果要从另一台启动了RMI注册服务的机器上查找hello实例
			// HelloInterface hello =
			// (HelloInterface)Naming.lookup("//192.168.1.105:1099/Hello");

			// 调用远程方法
			System.out.println(hello.echo("good morning"));
			System.out.println(hello.findUser("peter"));
		} catch (Exception e) {
			System.out.println("HelloClient exception: " + e);
		}
	}
}
