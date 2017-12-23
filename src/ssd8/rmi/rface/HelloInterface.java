package ssd8.rmi.rface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ssd8.rmi.bean.User;

/**
 * 远程接口必须扩展接口java.rmi.Remote 
 * 
 * @author wben
 *
 */
public interface HelloInterface extends Remote {
	
		/**  
	    * 远程接口方法必须抛出 java.rmi.RemoteException  
	    */  
	   public String echo(String msg) throws RemoteException; 
	   
	   public User findUser(String name) throws RemoteException;
}
