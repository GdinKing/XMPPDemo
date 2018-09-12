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
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

import java.io.IOException;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 14:03分
 * @since 2018-09-04
 * @author king
 */
public class XMPPService extends Service {


    public static AbstractXMPPConnection connection = null;

    private ConnectionListener connectionListener;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connection = XMPPHelper.getInstance().getConnection();
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
                if (e.getMessage() != null && e.getMessage().contains("conflict")) { //被挤掉线
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
                //i为重连倒计时
                Logger.i("来自连接监听,reconnectingIn:" + i);
            }

            @Override
            public void reconnectionFailed(Exception e) {
                Logger.i("来自连接监听,重连失败");
                Logger.e(e);
                connection.disconnect();
                try {
                    Thread.sleep(1500);
                    connection.connect();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        };
        connection.addConnectionListener(connectionListener);


        return START_STICKY;
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
        Logger.i("XMPPService结束了");
        super.onDestroy();
    }
}
