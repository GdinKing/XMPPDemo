package com.android.king.xmppdemo.event;

/**
 * 好友申请/状态 事件
 *
 * @author：King
 * @time: 2018/9/9 12:47
 */
public class FriendEvent {

    public String from;
    public String status;

    public FriendEvent(String frome,String status) {
        this.from = frome;
        this.status = status;
    }
}
