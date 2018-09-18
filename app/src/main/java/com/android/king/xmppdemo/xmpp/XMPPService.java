package com.android.king.xmppdemo.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.event.ConflictEvent;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.util.CommonUtil;
import com.android.king.xmppdemo.util.Logger;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ping.PingFailedListener;

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
                XMPPHelper.reconnectCount = 0;
            }

            @Override
            public void authenticated(XMPPConnection xmppConnection, boolean b) {
                XMPPHelper.reconnectCount = 0;
                XMPPHelper.getInstance().changeStatus(AppConstants.StanzaStatus.AVAILABLE);
                Logger.i("来自连接监听,登录成功");
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
                    EventBus.getDefault().post(new ConflictEvent(false));
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
                reconnect();
            }
        };

        connection.addConnectionListener(connectionListener);
        if (!connection.isConnected()) {
            reconnect();
        }
        //心跳
        XMPPHelper.getInstance().addPing(new PingFailedListener() {
            @Override
            public void pingFailed() {
                reconnect();
            }
        });
        return START_STICKY;
    }


    /**
     * 重连
     */
    private class ReconnectTask extends TimerTask {
        @Override
        public void run() {
            if (connection == null) {
                return;
            }
            if (!XMPPHelper.getInstance().isConnected()) {
                reconnect();
                return;
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

    /**
     * 重连
     */
    private void reconnect() {
        if(!CommonUtil.isNetAvailable(this)){
            return;
        }
        if (XMPPHelper.reconnectCount > 5) {
            //重连次数超过5次，退出app
            System.exit(-1);
            return;
        }
        XMPPHelper.reconnectCount += 1;
        AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                XMPPHelper.getInstance().reconnect();
                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {
                if (e != null) {
                    Logger.e(e);
                    //重连失败，
                    timer = new Timer();
                    timer.schedule(new ReconnectTask(), AppConstants.RECONNECT_DELAY);//延迟n秒后重连
                    return;
                }
                XMPPHelper.getInstance().addListeners();
            }
        });

    }
}
