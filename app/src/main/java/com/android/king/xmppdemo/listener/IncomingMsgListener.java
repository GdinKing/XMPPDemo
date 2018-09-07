package com.android.king.xmppdemo.listener;

import android.content.Context;
import android.content.Intent;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.util.Logger;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

/**
 * 消息接收监听
 */
public class IncomingMsgListener implements IncomingChatMessageListener {

    private Context context;

    public IncomingMsgListener(Context context) {
        this.context = context;
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message,
                                   Chat arg2) {
        Logger.i(from.toString() + "====" + message.getBody());
        ChatBean bean = new ChatBean();
        bean.setUser(from.toString());
        bean.setTime(System.currentTimeMillis());
        bean.setMessage(message.getBody());

        Intent intent = new Intent(AppConstants.ACTION_INCOME_MESSAGE);
        intent.putExtra("chat", bean);
        context.sendBroadcast(intent);
    }
}
