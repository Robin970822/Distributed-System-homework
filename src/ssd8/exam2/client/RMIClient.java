package ssd8.exam2.client;

import ssd8.exam2.rface.MessageInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

/**
 * RMI 客户端
 *
 * @author Hanxy
 * @version 1.0.0
 */
public class RMIClient {

    private static BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

    /**
     * 为维护方便，所有操作结果信息写在此处
     */
    private static final String WRONG_PARAMETER = "参数错误!";
    private static final String SUCCESS = "操作成功！";
    private static final String FAILURE = "操作失败！";
    private static final String TIME_FORMAT = "时间格式：yyyy-MM-dd-HH:mm:ss";

    private static final String REGISTER_SUCCESS = "注册成功！";
    private static final String REGISTER_FAILURE = "用户名已存在，请选择另一个用户名！";

    /**
     * 当前用户名与密码
     */
    private static String username;
    private static String password;

    /**
     * RMI 接口
     */
    static MessageInterface rmi;

    public static void main(String[] args) {
        /**
         * 创建远程对象
         */
        try {
            if (args.length < 3) {
                System.err.println(WRONG_PARAMETER);
                System.exit(0);
            }
            String host = args[0];
            String port = args[1];
            /*
            通过查找获得远程对象
             */
            rmi = (MessageInterface) Naming.lookup("//" + host + ":" + port + "/Message");

            /**
             * 注册服务
             */
            if (args[2].equals("register")) {
                if (args.length != 5) {
                    System.err.println(WRONG_PARAMETER);
                    System.exit(0);
                }
                String info = rmi.register(args[3], args[4]);
                if (info.equals(REGISTER_FAILURE)) {
                    System.err.println(REGISTER_FAILURE);
                } else {
                    username = args[3];
                    password = args[4];
                    System.out.println(username + REGISTER_SUCCESS);
                }
            } else {//其他服务
                username = args[3];
                password = args[4];
                String[] cmds = Arrays.copyOfRange(args, 5, args.length);
                service(cmds);
            }
            /**
             * 显示帮助
             */
            helpMenu();

            /**
             * 其他服务
             */
            while (true) {
                System.out.println("Input an operation:");
                String operation = bf.readLine();
                String[] cmds = operation.split(" ");
                service(cmds);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理命令
     *
     * @param cmds 命令
     */
    private static void service(String[] cmds) throws RemoteException {
        if (cmds[0].equals("show")){
            doShow();
        }else if (cmds[0].equals("check")){
            doCheck();
        }else if (cmds[0].equals("leave")){
            doLeave(cmds);
        }else if (cmds[0].equals("help")){
            helpMenu();
        }else if (cmds[0].equals("quit")){
            System.exit(0);
        }else System.err.println(WRONG_PARAMETER);
    }

    /**
     * 显示帮助菜单
     */
    private static void helpMenu() {
        System.out.println(TIME_FORMAT);
        System.out.println("HELP MENU:");
        System.out.println("\t" + "1.show");
        System.out.println("\t\t" + "arguments:no args");
        System.out.println("\t" + "2.check");
        System.out.println("\t\t" + "arguments:no args");
        System.out.println("\t" + "3.leave");
        System.out.println("\t\t" + "arguments:<receiver_name> <message_text>");
        System.out.println("\t" + "4.help");
        System.out.println("\t\t" + "arguments:no args");
        System.out.println("\t" + "5.quit");
        System.out.println("\t\t" + "arguments:no args");
    }

    private static void doShow() throws RemoteException {
        System.out.println(rmi.showUsers());
    }

    private static void doCheck() throws RemoteException {
        System.out.println(rmi.checkMessage(username, password));
    }

    private static void doLeave(String[] cmds) throws RemoteException {
        if (cmds.length != 3) {
            System.err.println(WRONG_PARAMETER);
        }else {
            System.out.println(rmi.leaveMessage(username, password, cmds[1], cmds[2]));
        }
    }
}
