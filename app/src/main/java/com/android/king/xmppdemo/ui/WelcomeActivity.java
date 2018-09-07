package com.android.king.xmppdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 14:52分
 * @since 2018-09-04
 * @author king
 */
public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        boolean isLogin = SPUtil.getBoolean(this, AppConstants.SP_KEY_LOGIN_STATUS,false);
//        if (isLogin) {
//            if(XMPPHelper.getInstance().isLogin()) {
//                startActivity(new Intent(this, MainActivity.class));
//            }else{
//                final String account = SPUtil.getString(this,AppConstants.SP_KEY_LOGIN_ACCOUNT);
//                final String password = SPUtil.getString(this,AppConstants.SP_KEY_LOGIN_PASSWOrD);
//                NetworkExecutor.getInstance().execute(new OnNetworkExecuteListener() {
//                    @Override
//                    public void onExecute() throws Exception {
//                        XMPPHelper.getInstance().getConnection().login(account,password);
//                    }
//
//                    @Override
//                    public void onFinish(Exception e) {
//                        if(e!=null){
//                            Logger.e(e);
//                            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
//                            return;
//                        }
//                        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
//                    }
//                });
//
//            }
//        }else{
            startActivity(new Intent(this, LoginActivity.class));
//        }
        finish();
    }
}
