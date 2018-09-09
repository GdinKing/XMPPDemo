package com.android.king.xmppdemo.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.android.king.xmppdemo.event.FriendEvent;
import com.android.king.xmppdemo.event.ReconnectErrorEvent;
import com.android.king.xmppdemo.listener.IncomingMsgListener;
import com.android.king.xmppdemo.util.Logger;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

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
    private ConnectionListener connectionListener;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connection = XMPPHelper.getInstance().getConnection();
        //重连
        ReconnectionManager manager = ReconnectionManager.getInstanceFor(connection);
        manager.setFixedDelay(2);//断线2秒重连
        manager.enableAutomaticReconnection();

        connectionListener = new ConnectionListener() {

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
            public void connectionClosedOnError(Exception e) {
                //这里就是网络不正常或者被挤掉断线激发的事件
                if (e == null) {
                    return;
                }
                if (e.getMessage()!=null&&e.getMessage().contains("conflict")){ //被挤掉线
                    //被人挤下线,重新弹出登录
                    Toast.makeText(getApplication(), "您的账号在别处登录", Toast.LENGTH_LONG).show();
                    EventBus.getDefault().post(new ReconnectErrorEvent());
                    stopSelf();
                }
                //这里smack会自动重连
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
        };
        connection.addConnectionListener(connectionListener);
        addFriendListener();

        if (incomingListener == null) {
            incomingListener = new IncomingMsgListener();
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
                    Logger.i("收到回复：" + p.getFrom() + "--" + p.getType());
                    String from = p.getFrom().toString();
                    String status = p.getType().toString();
                    EventBus.getDefault().post(new FriendEvent(from, status));
                }
            }

        };
        connection.addAsyncStanzaListener(listener, filter);
    }

//    private void reconnect() {
//        final String account = SPUtil.getString(this, AppConstants.SP_KEY_LOGIN_ACCOUNT);
//        final String password = SPUtil.getString(this, AppConstants.SP_KEY_LOGIN_PASSWORD);
//        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback<Void>() {
//            @Override
//            public Void onExecute() throws Exception {
//                XMPPHelper.getInstance().login(account, password, CommonUtil.getDeviceId(getApplication()));
//                return null;
//            }
//
//            @Override
//            public void onFinish(Void result, Exception e) {
//                if (e != null) {
//                    Logger.e(e);
//                    EventBus.getDefault().post(new ReconnectErrorEvent());
//                    return;
//                }
//            }
//        });
//
//
//    }


    @Override
    public void onDestroy() {
        if (connectionListener != null && connection != null) {
            connection.removeConnectionListener(connectionListener);
        }
        XMPPHelper.getInstance().logout();
        super.onDestroy();
    }
}
