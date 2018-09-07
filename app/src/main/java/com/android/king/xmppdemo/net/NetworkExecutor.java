package com.android.king.xmppdemo.net;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月05日 16:54分
 * @since 2018-09-05
 * @author king
 */
public class NetworkExecutor {


    private OnNetworkExecuteCallback callback;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (callback != null) {
                Exception e = null;
                if (msg.obj != null && msg.obj instanceof Exception) {
                    e = (Exception) msg.obj;
                }
                callback.onFinish(e);
            }
        }
    };

    public static NetworkExecutor getInstance() {
        return new NetworkExecutor();
    }

    public void execute(OnNetworkExecuteCallback listener) {
        this.callback = listener;
        AsyncHelper.getInstance().execute(new RunTask());
    }


    public class RunTask implements Runnable {
        @Override
        public void run() {
            try {
                if (callback != null) {
                    callback.onExecute();
                }
                handler.sendEmptyMessage(100);
            } catch (Exception e) {
                handler.obtainMessage(500, e).sendToTarget();
            }
        }
    }
}
