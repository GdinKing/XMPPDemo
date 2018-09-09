package com.android.king.xmppdemo.entity;

import java.io.Serializable;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 10:15分
 * @since 2018-09-04
 * @author king
 */
public class ChatBean implements Serializable{

    private int id;
    private String from;
    private long time;
    private String avatar;
    private String message;
    private String msgDb;
    private int unreadCount;
    private int type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMsgDb() {
        return msgDb;
    }

    public void setMsgDb(String msgDb) {
        this.msgDb = msgDb;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
