package com.android.king.xmppdemo.event;

import com.android.king.xmppdemo.entity.MessageBean;

/***
 * 发送消息事件
 * @since 2018-09-10
 * @author king
 */
public class SendMsgEvent {
    public MessageBean message;

    public SendMsgEvent(MessageBean message) {
        this.message = message;
    }
}
