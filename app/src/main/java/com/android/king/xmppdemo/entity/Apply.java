package com.android.king.xmppdemo.entity;

import java.io.Serializable;

/**
 * 好友申请
 * @author：King
 * @time: 2018/9/8 12:37
 */
public class Apply implements Serializable {

    public static final int STATUS_AGREED = 0;
    public static final int STATUS_IGNORE = -1;
    public static final int STATUS_UNAGREE = 1;

    private User user;
    private int status;

    public Apply(User user, int status) {
        this.user = user;
        this.status = status;
    }

    public Apply() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
