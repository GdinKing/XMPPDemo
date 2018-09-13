package com.android.king.xmppdemo.ui;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.fragment.HomeFragment;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SPUtil;
import com.android.king.xmppdemo.xmpp.XMPPHelper;
import com.android.king.xmppdemo.xmpp.XMPPJobService;
import com.android.king.xmppdemo.xmpp.XMPPService;

import me.yokeyword.fragmentation.SupportActivity;


/**
 * @author king
 */
public class MainActivity extends SupportActivity {


    private HomeFragment homeFragment;

    private JobScheduler mJobScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        //5.0+ 任务调度，Service保活
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1,
                    new ComponentName(getPackageName(), XMPPJobService.class.getName()));
            builder.setMinimumLatency(30 * 1000);
            if (mJobScheduler.schedule(builder.build()) <= 0) {
                Logger.e("任务调度出错");
            }
        }
    }


    private void init() {
        if (findFragment(HomeFragment.class) == null) {
            homeFragment = HomeFragment.newInstance();
            loadRootFragment(R.id.fl_container, homeFragment);
        }
        //判断是否登录过
        boolean isLogin = SPUtil.getBoolean(this, AppConstants.SP_KEY_LOGIN_STATUS, false);
        if (isLogin) {
            if (!XMPPHelper.getInstance().isLogin()) {//账号已经登录过，但xmpp未登录
                final String account = SPUtil.getString(this, AppConstants.SP_KEY_LOGIN_ACCOUNT);
                final String password = SPUtil.getString(this, AppConstants.SP_KEY_LOGIN_PASSWORD);
                NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback() {
                    @Override
                    public Object onExecute() throws Exception {
                        XMPPHelper.getInstance().getConnection().login(account, password);
                        return null;
                    }

                    @Override
                    public void onFinish(Object result, Exception e) {
                        if (e != null) {
                            Logger.e(e);
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                            return;
                        }
                        addListener();
                    }
                });
            } else {
                addListener();
            }
        }
    }

    /**
     * 监听器
     */
    private void addListener() {
        Intent intent = new Intent(this, XMPPService.class);
        startService(intent);
        XMPPHelper.getInstance().addMessageListener();
        XMPPHelper.getInstance().addInvitationListener();
        XMPPHelper.getInstance().addStanzaListener();
    }

    @Override
    public void onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport();
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mJobScheduler != null) {
                mJobScheduler.cancelAll();
            }
        }
        XMPPHelper.getInstance().logout();
        super.onDestroy();
    }

}
