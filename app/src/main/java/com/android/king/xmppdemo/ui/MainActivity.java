package com.android.king.xmppdemo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.king.xmppdemo.R;
import android.os.Bundle;
import android.widget.Toast;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.fragment.HomeFragment;
import com.android.king.xmppdemo.service.XMPPService;
import com.android.king.xmppdemo.util.Logger;

import me.yokeyword.fragmentation.SupportActivity;


/**
 * @author king
 */
public class MainActivity extends SupportActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Intent intent = new Intent(this, XMPPService.class);
        startService(intent);

        IntentFilter msgFilter = new IntentFilter(AppConstants.ACTION_INCOME_MESSAGE);
        registerReceiver(messageReceiver, msgFilter);


    }

    private void init() {
        if (findFragment(HomeFragment.class) == null) {
            loadRootFragment(R.id.fl_container, HomeFragment.newInstance());
        }

    }


    /**
     * 收到新消息监听
     */
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i("有新消息来啦！");
            ChatBean bean = (ChatBean) intent.getSerializableExtra("chat");
            if (bean == null) {
                return;
            }
            Logger.i(bean.getMessage());
            Toast.makeText(MainActivity.this, bean.getUser() + ":" + bean.getMessage(), Toast.LENGTH_LONG).show();
        }
    };




    @Override
    public void onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(messageReceiver);
        super.onDestroy();
    }
}
