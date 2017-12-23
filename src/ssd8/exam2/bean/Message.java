package ssd8.exam2.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息实体类
 *
 * @author Hanxy
 * @version 1.0.0
 * @see java.io.Serializable
 */
public class Message implements Serializable{

    private String senderName;
    private String receiverName;
    private String date;
    private String text;

    public Message(String senderName, String receiverName, String date, String text) {
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.date = date;
        this.text = text;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;

        if (!getSenderName().equals(message.getSenderName())) return false;
        if (!getReceiverName().equals(message.getReceiverName())) return false;
        if (!getDate().equals(message.getDate())) return false;
        return getText() != null ? getText().equals(message.getText()) : message.getText() == null;
    }

    @Override
    public String toString() {
        return "Message{" +
                "senderName='" + senderName + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", date=" + date +
                ", text='" + text + '\'' +
                '}';
    }
}
