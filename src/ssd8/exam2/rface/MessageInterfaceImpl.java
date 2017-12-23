package ssd8.exam2.rface;

import ssd8.exam2.bean.Message;
import ssd8.exam2.bean.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 消息系统接口实现
 *
 * @author Hanxy
 * @version 1.0.0
 * @see ssd8.exam2.rface.MessageInterface
 * @see java.rmi.server.UnicastRemoteObject
 */
public class MessageInterfaceImpl extends UnicastRemoteObject implements MessageInterface {


    private static ArrayList<User> users = new ArrayList<>();
    private static ArrayList<Message> messages = new ArrayList<>();

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

    /**
     * 为维护方便，所有返回信息写在此处
     */
    private static final String CRLF = "\r\n";
    private static final String REGISTER_SUCCESS = "注册成功！";
    private static final String REGISTER_FAILURE = "用户名已存在，请选择另一个用户名！";
    private static final String LEAVE_SUCCESS = "留言成功！";

    private static final String ACCOUNT_ERROR = "用户名或密码错误";

    private static final String NO_USERS = "系统还未注册用户";

    private static final String NO_USER = "系统没有该用户";
    private static final String NO_MESSAGE = "没有有留言";

    /**
     * 构造函数
     *
     * @throws RemoteException
     */
    public MessageInterfaceImpl() throws RemoteException {
        super();
    }

    /**
     * 注册用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 注册是否成功
     * @throws RemoteException
     */
    @Override
    public String register(String username, String password) throws RemoteException {
        /*
        注册失败
         */
        if (isUserExist(username)){
            return REGISTER_FAILURE;
        }
        /*
        注册成功
         */
        User user = new User(username, password);
        users.add(user);
        return REGISTER_SUCCESS;
    }

    /**
     * 显示所有注册用户
     *
     * @return 所有注册用户名列表
     * @throws RemoteException
     */
    @Override
    public String showUsers() throws RemoteException {
        /*
        没有用户
         */
        if (users.isEmpty()){
            return NO_USERS;
        }else {//有用户
            String info = "";
            for (User user : users) {
                info += user.getUsername();
                info += CRLF;
            }
            return info;
        }
    }

    /**
     * 显示用户所有留言
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户所有留言列表
     * @throws RemoteException
     */
    @Override
    public String checkMessage(String username, String password) throws RemoteException {
        String info = "";
        boolean hasMessage = false;
        /*
        判断用户账户
         */
        if (!isUserCorrect(username, password)){
            return ACCOUNT_ERROR;
        }else {

            for (Message message : messages){
                if (message.getReceiverName().equals(username)){
                    hasMessage = true;
                    info += message.toString();
                    info += CRLF;
                }
            }
        }
        /*
        判断是否有留言
         */
        if (hasMessage){
            return info;
        }else return NO_MESSAGE;
    }

    /**
     * 留言
     *
     * @param username      用户名
     * @param password      密码
     * @param receiver_name 接收者
     * @param message_txt   留言信息
     * @return 留言返回消息
     * @throws RemoteException
     */
    @Override
    public String leaveMessage(String username, String password, String receiver_name, String message_txt) throws RemoteException {
        /*
        判断用户账号与接收者账号
         */
        if (!isUserCorrect(username, password)){
            return ACCOUNT_ERROR;
        }else if (!isUserExist(receiver_name)){
            return NO_USER;
        }
        /*
        留言
         */
        Message message = new Message(username, receiver_name, dateFormat.format(new Date()), message_txt);
        messages.add(message);
        return LEAVE_SUCCESS;
    }

    private boolean isUserCorrect(String username, String password){
        User currentUser = new User(username, password);
        for (User user : users) {
            if (currentUser.equals(user)){
                return true;
            }
        }
        return false;
    }

    private boolean isUserExist(String username){
        for (User user: users){
            if (user.getUsername().equals(username)){
                return true;
            }
        }
        return false;
    }
}
