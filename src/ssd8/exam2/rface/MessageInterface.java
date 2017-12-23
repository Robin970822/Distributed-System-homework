package ssd8.exam2.rface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 消息系统接口
 *
 * @author Hanxy
 * @version 1.0.0
 * @see java.rmi.Remote
 */
public interface MessageInterface extends Remote{

    /**
     * 注册用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 注册是否成功
     * @throws RemoteException
     */
    public String register(String username, String password) throws RemoteException;

    /**
     * 显示所有注册用户
     *
     * @return 所有注册用户名列表
     * @throws RemoteException
     */
    public String showUsers() throws RemoteException;

    /**
     * 显示用户所有留言
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户所有留言列表
     * @throws RemoteException
     */
    public String checkMessage(String username, String password) throws RemoteException;

    /**
     * 留言
     *
     * @param username 用户名
     * @param password 密码
     * @param receiver_name 接收者
     * @param message_txt 留言信息
     * @return 留言返回消息
     * @throws RemoteException
     */
    public String leaveMessage(String username, String password, String receiver_name, String message_txt) throws RemoteException;
}
