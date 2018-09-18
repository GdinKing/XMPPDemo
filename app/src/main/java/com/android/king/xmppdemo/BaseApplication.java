package com.android.king.xmppdemo;

import android.app.Application;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.db.SQLiteHelper;
import com.android.king.xmppdemo.util.CrashHandler;
import com.android.king.xmppdemo.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.Fragmentation;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 10:59分
 * @since 2018-09-04
 * @author king
 */
public class BaseApplication extends Application {

    public static BaseApplication instance;

    public static BaseApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //崩溃日志记录
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        //初始化Fragmentation
        Fragmentation.builder()
                .stackViewMode(Fragmentation.NONE)
                .debug(BuildConfig.DEBUG)
                .install();

        //初始化数据库
        List<String> tableList = new ArrayList<>();
        tableList.add(AppConstants.CREATE_TABLE_APPLY);
        tableList.add(AppConstants.CREATE_TABLE_CHAT);
        tableList.add(AppConstants.CREATE_TABLE_FRIEND);
        SQLiteHelper.init(this, tableList);
    }


    /**
     * 获取当前登录用户
     *
     * @return
     */
    public static String getCurrentLogin() {
        return SPUtil.getString(instance, AppConstants.SP_KEY_LOGIN_ACCOUNT);
    }

    /**
     * 获取当前登录用户密码
     * 这里是不安全操作，实际开发中并不这样做，我这里为了方便
     *
     * @return
     */
    public static String getLoginPassword() {
        return SPUtil.getString(instance, AppConstants.SP_KEY_LOGIN_PASSWORD);
    }
}
