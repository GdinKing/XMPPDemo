package com.android.king.xmppdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.util.SPUtil;

/***
 * 欢迎界面
 * @since 2018-09-04
 * @author king
 */
public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isLogin = SPUtil.getBoolean(this, AppConstants.SP_KEY_LOGIN_STATUS, false);
        if (isLogin) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
