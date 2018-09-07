package com.android.king.xmppdemo.net;

import android.annotation.SuppressLint;
import com.android.king.xmppdemo.listener.OnNetworkExecuteListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月05日 16:54分
 * @since 2018-09-05
 * @author king
 */
public class NetworkExecutor {

    private ExecutorService executorService;

    private OnNetworkExecuteListener executeListener;

    private NetworkExecutor() {
        int num = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(num * 2);
    }

    private static NetworkExecutor instance;

    public static NetworkExecutor getInstance() {
        if (instance == null) {
            synchronized (NetworkExecutor.class) {
                if (instance == null) {
                    instance = new NetworkExecutor();
                }
            }
        }
        return instance;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (executeListener != null) {
                Exception e = null;
                if (msg.obj != null) {
                    e = (Exception) msg.obj;
                }
                executeListener.onFinish(e);
            }
        }
    };

    public void execute(final OnNetworkExecuteListener listener) {
        this.executeListener = listener;
        if(executorService==null){
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (listener != null) {
                        listener.onExecute();
                    }
                    handler.sendEmptyMessage(100);
                } catch (Exception e) {
                    handler.obtainMessage(101, e).sendToTarget();
                }
            }
        });
    }

    public void execute(Runnable runnable) {
        if(executorService!=null) {
            executorService.execute(runnable);
        }
    }

    public void cancel() {
        executorService.shutdownNow();
        handler.removeCallbacksAndMessages(null);
    }
}
