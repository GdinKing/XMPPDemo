package com.android.king.xmppdemo.listener;

import android.database.sqlite.SQLiteDatabase;

/**
 * @author：King
 * @time: 2018/9/8 13:27
 */
public interface OnSqliteUpdateListener {
    public void onSqliteUpdateListener(SQLiteDatabase db, int oldVersion, int newVersion);
}
