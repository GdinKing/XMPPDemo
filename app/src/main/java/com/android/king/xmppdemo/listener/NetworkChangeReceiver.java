package com.android.king.xmppdemo.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;
import com.android.king.xmppdemo.xmpp.XMPPService;

/***
 * 网络状态监听
 * @since 2018-09-14
 * @author king
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        Logger.i("当前WiFi连接可用 ");
                        Intent i = new Intent(context, XMPPService.class);
                        context.startService(i);
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        Logger.i("当前移动网络连接可用 ");
                        Intent i = new Intent(context, XMPPService.class);
                        context.startService(i);
                    }
                } else {
                    Looper.prepare();
                    Toast.makeText(context, "网络连接断开", Toast.LENGTH_LONG).show();
                    Looper.loop();
                    Logger.e("当前没有网络连接，请确保你已经打开网络 ");
                }

            } else {
                Logger.e("当前没有网络连接，请确保你已经打开网络 ");
                Looper.prepare();
                Toast.makeText(context, "网络连接断开", Toast.LENGTH_LONG).show();
                Looper.loop();
            }


        }
    }
}
