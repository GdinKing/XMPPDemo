package com.android.king.xmppdemo.entity;

import java.io.Serializable;

/***
 * 消息实体
 * @since 2018-09-04
 * @author king
 */
public class MessageBean implements Serializable{

    private int id;
    private String from;
    private String to;
    private long time;
    private String content;
    private int type;
    private int category;
    private int status;
    private String msgDb;
    private String avatar;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMsgDb() {
        return msgDb;
    }

    public void setMsgDb(String msgDb) {
        this.msgDb = msgDb;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
