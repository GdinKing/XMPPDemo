package com.android.king.xmppdemo.entity;

import android.graphics.Bitmap;

import java.io.Serializable;

/***
 * 对话实体
 * @since 2018-09-04
 * @author king
 */
public class ChatBean implements Serializable{

    private int id;
    private String title;
    private String target;
    private long time;
    private int avatar = 0;  //头像资源ID
    private String message;
    private String msgDb;
    private int unreadCount;
    private int type;
    private String action; //对应的操作

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
