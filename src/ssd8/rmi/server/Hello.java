package ssd8.rmi.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ssd8.rmi.bean.User;
import ssd8.rmi.rface.HelloInterface;

/**
 * 扩展UnicastRemoteObject类，并实现远程接口HelloInterface 
 * 
 * @author wben
 *
 */
public class Hello extends UnicastRemoteObject implements HelloInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String status = "0000";
	
	private List<User> userList = new ArrayList<User>();
	
	/**
	 * 必须定义构造方法，即使是默认构造方法，也必须把它明确地写出来，因为它必须抛出出RemoteException异常
	 */
	public Hello() throws RemoteException {
		
		userList.add(new User("peter","peter"));
	}

	/**
	 * 远程接口方法的实现
	 */
	public String echo(String msg) throws RemoteException {
		System.out.println("Called by HelloClient");
		status = status + msg;
		System.out.println(getStatus());
		return "[rmi echo]: " + msg;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public User findUser(String name) throws RemoteException {
		return userList.get(0);
	}
	
	
}
