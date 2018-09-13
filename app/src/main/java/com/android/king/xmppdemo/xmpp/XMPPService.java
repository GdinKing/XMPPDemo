package com.android.king.xmppdemo.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.android.king.xmppdemo.BaseApplication;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.event.FriendEvent;
import com.android.king.xmppdemo.event.ReconnectErrorEvent;
import com.android.king.xmppdemo.listener.IncomingMsgListener;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
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
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/***
 * 保持XMPP连接的服务
 * 包含发送心跳和断线重连
 * @since 2018-09-04
 * @author king
 */
public class XMPPService extends Service {


    private Timer timer;

    public static AbstractXMPPConnection connection = null;

    private ConnectionListener connectionListener;

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {

        public XMPPService getService() {
            return XMPPService.this;
        }
    }

    private MyBinder myBinder = new MyBinder();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i("服务启动了");
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
                NetworkExecutor.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            XMPPHelper.getInstance().reconnect();
                        } catch (Exception e1) {
                            Logger.e(e1);
                        }
                    }
                });

            }
        };
        connection.addConnectionListener(connectionListener);

        try {
            timer = new Timer();
            timer.schedule(new HeartBeatTask(), AppConstants.HEART_BEAT, AppConstants.HEART_BEAT);
        } catch (Exception e) {

        }

        return START_STICKY;
    }


    /**
     * 心跳连接
     */
    private class HeartBeatTask extends TimerTask {
        @Override
        public void run() {
            if (connection == null) {
                return;
            }
            if (!connection.isConnected()) {
                reconnect();
                return;
            }
            PingManager.getInstanceFor(connection).registerPingFailedListener(new PingFailedListener() {
                @Override
                public void pingFailed() {
                    reconnect();
                }
            });
            try {
                Logger.i("心跳连接:" + new Date().toLocaleString());
                PingManager.getInstanceFor(connection).pingAsync(connection.getUser());//ping自己。让服务器认为你不是空闲连接
            } catch (Exception e) {
                Logger.e(e);
            }
        }
    }


    @Override
    public void onDestroy() {
        Logger.i("XMPPService结束了");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }

    private void reconnect() {
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                XMPPHelper.getInstance().reconnect();
                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {
                if (e != null) {
                    Logger.e(e);
                    //退出app
                    System.exit(-1);
                }
            }
        });

    }
}
