package com.android.king.xmppdemo.event;

import com.android.king.xmppdemo.entity.ChatBean;

/**
 * 接收消息事件
 * @author：King
 * @time: 2018/9/9 13:05
 */
public class MessageEvent {

    public ChatBean chatBean;

    public MessageEvent(ChatBean chatBean) {
        this.chatBean = chatBean;
    }
}
