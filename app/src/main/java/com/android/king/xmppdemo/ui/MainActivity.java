package com.android.king.xmppdemo.ui;

import android.content.Intent;
import android.os.Bundle;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.fragment.HomeFragment;
import com.android.king.xmppdemo.fragment.UserFragment;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.PingTask;
import com.android.king.xmppdemo.xmpp.XMPPHelper;
import com.android.king.xmppdemo.xmpp.XMPPService;

import org.jivesoftware.smackx.ping.PingFailedListener;

import java.util.Timer;

import me.yokeyword.fragmentation.SupportActivity;


/**
 * @author king
 */
public class MainActivity extends SupportActivity {


    private HomeFragment homeFragment;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Intent intent = new Intent(this, XMPPService.class);
        startService(intent);
        XMPPHelper.getInstance().addMessageListener();
        XMPPHelper.getInstance().addStanzaListener();
        XMPPHelper.getInstance().addHeartBeat(new PingFailedListener() {
            @Override
            public void pingFailed() {
                try {
                    timer = new Timer();
                    timer.schedule(new PingTask(timer, AppConstants.RECONNECT_DELAY), AppConstants.RECONNECT_DELAY);
                } catch (Exception e) {
                    Logger.e(e);
                }
            }

        });
    }


    private void init() {
        if (findFragment(HomeFragment.class) == null) {
            homeFragment = HomeFragment.newInstance();
            loadRootFragment(R.id.fl_container, homeFragment);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        if()
//        homeFragment.startFragment(UserFragment.newInstance(user.getAccount(), user.getNote()));

    }

    @Override
    public void onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        XMPPHelper.getInstance().logout();
        super.onDestroy();
    }
}
