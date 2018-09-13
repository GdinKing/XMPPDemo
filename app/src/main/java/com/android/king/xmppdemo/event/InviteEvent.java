package com.android.king.xmppdemo.event;

import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.entity.MessageBean;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.EntityJid;

/***
 * 加入群聊邀请事件
 * @since 2018-09-13
 * @author king
 */
public class InviteEvent {
    public ChatBean chatBean;

    public InviteEvent(ChatBean chatBean) {
        this.chatBean = chatBean;
    }
}
