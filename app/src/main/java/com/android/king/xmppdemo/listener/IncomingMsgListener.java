package com.android.king.xmppdemo.listener;

import android.text.TextUtils;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.event.MessageEvent;
import com.android.king.xmppdemo.util.Logger;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

/**
 * 消息接收监听
 */
public class IncomingMsgListener implements IncomingChatMessageListener {

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message,
                                   Chat arg2) {
        Logger.i(from.toString() + "====" + message.getBody());
        if (TextUtils.isEmpty(message.getBody())) {
            return;
        }
        ChatBean bean = new ChatBean();
        bean.setTarget(from.toString());
        bean.setTime(System.currentTimeMillis());
        bean.setMessage(message.getBody());
        bean.setType(AppConstants.ChatType.SINGLE);
        bean.setMsgDb(from.toString().split("@")[0]);

        EventBus.getDefault().post(new MessageEvent(bean));
    }
}
