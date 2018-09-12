package com.android.king.xmppdemo.xmpp;

import android.content.Intent;
import android.util.Log;

import com.android.king.xmppdemo.util.Logger;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.Timer;
import java.util.TimerTask;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月12日 17:49分
 * @since 2018-09-12
 * @author king
 */
public class PingTask extends TimerTask {
    private Timer reConnectTimer;
    private AbstractXMPPConnection mConnection;

    private int delay = 10000;

    //pingFailed时启动重连线程
    public PingTask(Timer timer, int delay) {
        this.delay = delay;
        this.reConnectTimer = timer;
    }

    @Override
    public void run() {
        // 无网络连接时,直接返回
//            if (getNetworkState(mService) == NETWORN_NONE) {
//                Log.i(TAG, "无网络连接，"+delay/1000+"s后重新连接");
//                reConnectTimer.schedule(new ReConnectTimer(), delay);
//                //reConnectTimer.cancel();
//                return;
//            }
        // 连接服务器
        try {
            XMPPHelper.getInstance().reconnect();
            reConnectTimer.cancel();
        } catch (Exception e) {
            Logger.i("重连失败，" + delay / 1000 + "s后重新连接");
            reConnectTimer.schedule(new PingTask(reConnectTimer, delay), delay);
        }

    }
}
