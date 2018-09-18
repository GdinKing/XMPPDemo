package com.android.king.xmppdemo.net;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.king.xmppdemo.listener.OnExecuteCallback;

/***
 * 异步执行工具
 * @since 2018-09-05
 * @author king
 */
public class AsyncExecutor<T> {


    private OnExecuteCallback<T> callback;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (callback != null) {
                Exception e = null;
                if (msg.obj != null && msg.obj instanceof Exception) {
                    e = (Exception) msg.obj;
                    callback.onFinish(null, e);
                } else {
                    callback.onFinish((T) msg.obj, null);
                }
            }
        }
    };

    public static AsyncExecutor getInstance() {
        return new AsyncExecutor();
    }

    public void execute(OnExecuteCallback listener) {
        this.callback = listener;
        AsyncHelper.getInstance().execute(new RunTask());
    }
    public void execute(Runnable runnable) {
        AsyncHelper.getInstance().execute(runnable);
    }

    public class RunTask implements Runnable {
        @Override
        public void run() {
            try {
                if (callback != null) {
                    T object = callback.onExecute();
                    handler.obtainMessage(100, object).sendToTarget();
                }
            } catch (Exception e) {
                handler.obtainMessage(500, e).sendToTarget();
            }
        }
    }
}
