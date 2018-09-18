package com.android.king.xmppdemo.ui;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.event.LoginEvent;
import com.android.king.xmppdemo.event.ConflictEvent;
import com.android.king.xmppdemo.fragment.HomeFragment;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SPUtil;
import com.android.king.xmppdemo.xmpp.XMPPHelper;
import com.android.king.xmppdemo.xmpp.XMPPJobService;
import com.android.king.xmppdemo.xmpp.XMPPService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
        EventBus.getDefault().register(this);
    }


    private void init() {
        if (findFragment(HomeFragment.class) == null) {
            homeFragment = HomeFragment.newInstance();
            loadRootFragment(R.id.fl_container, homeFragment);
        }

    }


    /**
     * 挤下线回调
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReconnectEvent(ConflictEvent event) {
        if(!event.isConnected) {
            Toast.makeText(this, "您的账号在别处登录", Toast.LENGTH_LONG).show();
            XMPPHelper.getInstance().logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }


    @Override
    public void onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mJobScheduler != null) {
                mJobScheduler.cancelAll();
            }
        }
        XMPPHelper.getInstance().logout();
        super.onDestroy();
    }

}
