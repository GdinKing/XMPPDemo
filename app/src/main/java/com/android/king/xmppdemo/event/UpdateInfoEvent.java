package com.android.king.xmppdemo.event;

import com.android.king.xmppdemo.entity.User;

/**
 * @author：King
 * @time: 2018/9/13 19:42
 */
public class UpdateInfoEvent {

    public User user;

    public UpdateInfoEvent(User user) {
        this.user = user;
    }
}
