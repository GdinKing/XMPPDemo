package com.android.king.xmppdemo.util;

import android.util.Log;

import com.android.king.xmppdemo.BuildConfig;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 14:09分
 * @since 2018-09-04
 * @author king
 */
public class Logger {


    public static String TAG = "king";
    public static boolean DEBUG = BuildConfig.DEBUG;


    public static void i(String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void e(Throwable e) {
        if (DEBUG && e != null) {
            if (e.getCause() != null) {

                Log.e(TAG, e.getCause().toString(), e);
            } else {
                Log.e(TAG, "", e);
            }
        }
    }
}
