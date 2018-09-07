package com.android.king.xmppdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.listener.IncomingMsgListener;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 14:03分
 * @since 2018-09-04
 * @author king
 */
public class XMPPService extends Service {

    private ChatManager chatManager;
    private OfflineMessageManager offlineManager;
    public static AbstractXMPPConnection connection = null;

    private IncomingMsgListener incomingListener = null;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connection = XMPPHelper.getInstance().getConnection();
        //重连
        ReconnectionManager manager = ReconnectionManager.getInstanceFor(connection);
        manager.setFixedDelay(3);//断线3秒重连
        manager.enableAutomaticReconnection();
        connection.addConnectionListener(new ConnectionListener() {

            @Override
            public void connected(XMPPConnection xmppConnection) {

            }

            @Override
            public void authenticated(XMPPConnection xmppConnection, boolean b) {

            }

            @Override
            public void connectionClosed() {
                Logger.i("来自连接监听,conn正常关闭");
            }

            @Override
            public void connectionClosedOnError(Exception arg0) {
                //这里就是网络不正常或者被挤掉断线激发的事件
                if (arg0.getMessage().contains("conflict")) { //被挤掉线
                /*              log.e("来自连接监听,conn非正常关闭");
                                log.e("非正常关闭异常:"+arg0.getMessage());
                                log.e(con.isConnected());*/
                    //关闭连接，由于是被人挤下线，可能是用户自己，所以关闭连接，让用户重新登录是一个比较好的选择
                    XMPPHelper.getInstance().logout();
                    //接下来你可以通过发送一个广播，提示用户被挤下线，重连很简单，就是重新登录
                } else if (arg0.getMessage().contains("Connection timed out")) {//连接超时
                    // 不做任何操作，会实现自动重连
                }
            }

            @Override
            public void reconnectionSuccessful() {
                try {
                    Presence presence = new Presence(Presence.Type.available);//在线
                    connection.sendStanza(presence);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void reconnectingIn(int i) {
                Logger.i("来自连接监听,reconnectingIn:" + i);
            }

            @Override
            public void reconnectionFailed(Exception e) {
                Logger.i("来自连接监听,重连失败");
                Logger.e(e);
            }

        });
        addFriendListener();

        if (incomingListener == null) {
            incomingListener = new IncomingMsgListener(this);
        }
        if (chatManager == null) {
            chatManager = ChatManager.getInstanceFor(connection);
            chatManager.addIncomingListener(incomingListener);
        }
        return START_STICKY;
    }

    /**
     * 好友申请监听
     */
    public void addFriendListener() {
        //条件过滤
        StanzaFilter filter = new AndFilter(new StanzaTypeFilter(Presence.class));
        StanzaListener listener = new StanzaListener() {
            @Override
            public void processStanza(Stanza stanza) {
                Logger.i(stanza.toString());
                if (stanza instanceof Presence) {
                    Presence p = (Presence) stanza;
                    Logger.i("收到回复："+p.getFrom() + "--" + p.getType());
                    Intent intent = new Intent(AppConstants.ACTION_FRIEND);
                    intent.putExtra(AppConstants.INTENT_KEY_ADD_FRIEND_FROM, p.getFrom().toString());

                    String status = p.getType().toString();
                    switch (status){
                        case AppConstants.FriendStatus.SUBSCRIBE://收到好友请求
                            intent.putExtra(AppConstants.INTENT_KEY_ADD_FRIEND, AppConstants.STATUS_ADD_FRIEND_RECEIVE);
                            break;
                        case AppConstants.FriendStatus.SUBSCRIBED://好友申请通过
                            intent.putExtra(AppConstants.INTENT_KEY_ADD_FRIEND, AppConstants.STATUS_ADD_FRIEND_OK);
                            break;
                        case AppConstants.FriendStatus.UNSUBSCRIBE://好友申请拒绝
                            intent.putExtra(AppConstants.INTENT_KEY_ADD_FRIEND, AppConstants.STATUS_ADD_FRIEND_REJECJ);
                            break;
                    }
                    sendBroadcast(intent);
                }
            }

        };
        connection.addAsyncStanzaListener(listener, filter);
    }


    public void sendUserMsg(Message.Type type, String subject,
                            String user, String body) throws Exception {
        if (chatManager == null) {
            chatManager = ChatManager.getInstanceFor(connection);
            chatManager.addIncomingListener(incomingListener);
        }
        EntityBareJid groupJid = JidCreate.entityBareFrom(user);
        Chat chat = chatManager.chatWith(groupJid);
        Message msg = new Message();
        msg.setType(type);
        msg.setSubject(subject);
        msg.setBody(body);
        chat.send(msg);
    }


    @Override
    public void onDestroy() {
        XMPPHelper.getInstance().logout();
        super.onDestroy();
    }
}
