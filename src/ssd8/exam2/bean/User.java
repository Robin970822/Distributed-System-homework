package ssd8.exam2.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 用户实体类
 *
 * @author Hanxy
 * @version 1.0.0
 * @see java.io.Serializable
 */
public class User implements Serializable{

    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!getUsername().equals(user.getUsername())) return false;
        return getPassword().equals(user.getPassword());
    }
}
