package com.android.king.xmppdemo.entity;

import android.graphics.Bitmap;

import java.io.Serializable;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 16:44分
 * @since 2018-09-04
 * @author king
 */
public class User implements Serializable {

    private String name;
    private String nickName;
    private Bitmap avatar;
    private String account;
    private String group;
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
