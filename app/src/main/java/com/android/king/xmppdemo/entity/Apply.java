package com.android.king.xmppdemo.entity;

import java.io.Serializable;

/**
 * 好友申请
 * @author：King
 * @time: 2018/9/8 12:37
 */
public class Apply implements Serializable {

    private User user;
    private boolean isAgree;

    public Apply(User user, boolean isAgree) {
        this.user = user;
        this.isAgree = isAgree;
    }

    public Apply() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isAgree() {
        return isAgree;
    }

    public void setAgree(boolean agree) {
        isAgree = agree;
    }
}
