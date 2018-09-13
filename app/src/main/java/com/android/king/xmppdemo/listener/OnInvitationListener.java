package com.android.king.xmppdemo.listener;

import android.graphics.BitmapFactory;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.event.InviteEvent;
import com.android.king.xmppdemo.util.Logger;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jxmpp.jid.EntityJid;

/***
 * 群聊邀请监听
 * @since 2018-09-13
 * @author king
 */
public class OnInvitationListener implements InvitationListener {
    @Override
    public void invitationReceived(XMPPConnection xmppConnection, MultiUserChat multiUserChat, EntityJid entityJid, String s, String s1, Message message, MUCUser.Invite invite) {
        Logger.i("群聊邀请：");
        Logger.i("message:" + message.toString());
        Logger.i("entityJid" + entityJid.toString());
        Logger.i("MultiUserChat" + multiUserChat.toString());
        Logger.i("s:" + s);

        ChatBean chatBean = new ChatBean();
        chatBean.setTitle("群聊邀请");
        chatBean.setTarget(entityJid.toString());
        chatBean.setTime(System.currentTimeMillis());
        chatBean.setMessage(s);
        chatBean.setAvatar(R.mipmap.ic_launcher);
        chatBean.setUnreadCount(1);
        chatBean.setType(AppConstants.ChatType.MULTI_INVITE);
        EventBus.getDefault().post(new InviteEvent(chatBean));
    }
}
