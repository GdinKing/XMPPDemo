package com.android.king.xmppdemo.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author：King
 * @time: 2018/9/7 20:37
 */
public class AsyncHelper {


    private ExecutorService executorService;
    private static AsyncHelper instance;

    private AsyncHelper() {
        int num = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(num * 2);
    }


    public static AsyncHelper getInstance() {
        if (instance == null) {
            synchronized (NetworkExecutor.class) {
                if (instance == null) {
                    instance = new AsyncHelper();
                }
            }
        }
        return instance;
    }



    public void execute(Runnable runnable) {
        if (executorService != null) {
            executorService.execute(runnable);
        }
    }

    public void close() {
        executorService.shutdownNow();
    }

}
