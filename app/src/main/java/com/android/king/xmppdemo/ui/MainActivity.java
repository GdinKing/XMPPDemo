package com.android.king.xmppdemo.ui;

import android.content.Intent;
import android.os.Bundle;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.fragment.HomeFragment;
import com.android.king.xmppdemo.xmpp.XMPPHelper;
import com.android.king.xmppdemo.xmpp.XMPPService;

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

    }

    private void init() {
        if (findFragment(HomeFragment.class) == null) {
            loadRootFragment(R.id.fl_container, HomeFragment.newInstance());
        }

    }

    @Override
    public void onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport();
    }

    @Override
    protected void onDestroy() {
        XMPPHelper.getInstance().logout();
        super.onDestroy();
    }
}
