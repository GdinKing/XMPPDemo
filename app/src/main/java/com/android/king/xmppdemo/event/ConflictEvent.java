package com.android.king.xmppdemo.event;

/**
 * 挤下线事件
 *
 * @author：King
 * @time: 2018/9/9 12:54
 */
public class ConflictEvent {

    public boolean isConnected;

    public ConflictEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
