package com.android.king.xmppdemo;

import android.app.Application;
import android.king.xmppdemo.BuildConfig;

import me.yokeyword.fragmentation.Fragmentation;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 10:59分
 * @since 2018-09-04
 * @author king
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fragmentation.builder()
                .stackViewMode(Fragmentation.NONE)
                .debug(BuildConfig.DEBUG)
                .install();
    }
}