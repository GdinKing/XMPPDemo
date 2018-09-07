package com.android.king.xmppdemo.ui;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.king.xmppdemo.R;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.fragment.HomeFragment;
import com.android.king.xmppdemo.listener.OnNetworkExecuteListener;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.service.XMPPService;
import com.android.king.xmppdemo.util.CommonUtil;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SPUtil;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import me.yokeyword.fragmentation.SupportActivity;


/**
 * @author king
 */
public class MainActivity extends SupportActivity {

    private boolean isOnTop = false;
    private String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Intent intent = new Intent(this, XMPPService.class);
        startService(intent);

        IntentFilter filter = new IntentFilter(AppConstants.ACTION_FRIEND);
        registerReceiver(addFriendReceiver, filter);

        IntentFilter msgFilter = new IntentFilter(AppConstants.ACTION_INCOME_MESSAGE);
        registerReceiver(messageReceiver, msgFilter);

        IntentFilter applyFilter = new IntentFilter();
        applyFilter.addAction(CommonUtil.ACTION_RECEIVE_NOTICE);
        registerReceiver(applyReceiver, applyFilter);

    }

    private void init() {
        if (findFragment(HomeFragment.class) == null) {
            loadRootFragment(R.id.fl_container, HomeFragment.newInstance());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        isOnTop = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isOnTop = false;
    }


    /**
     * 添加好友监听
     */
    private BroadcastReceiver addFriendReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int flag = intent.getIntExtra(AppConstants.INTENT_KEY_ADD_FRIEND, -1);
            from = intent.getStringExtra(AppConstants.INTENT_KEY_ADD_FRIEND_FROM);
            if (flag == AppConstants.STATUS_ADD_FRIEND_OK) {
                Toast.makeText(MainActivity.this, "对方通过了你的好友申请！", Toast.LENGTH_LONG).show();
            } else if (flag == AppConstants.STATUS_ADD_FRIEND_RECEIVE) {
                if (isOnTop) {
                    showApplyDialog(from);
                } else {
                    CommonUtil.showNotify(MainActivity.this, from.split("@")[0] + "请求加你为好友");
                }
            } else if (flag == AppConstants.STATUS_ADD_FRIEND_REJECJ) {
                Toast.makeText(MainActivity.this, "对方拒绝了你的好友申请！", Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * 弹出请求添加好友对话框
     *
     * @param from
     */
    private void showApplyDialog(final String from) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton("通过", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                acceptRejectApply(0);
            }
        });
        builder.setMessage(from + "请求加你为好友");
        builder.setNeutralButton("拒绝", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                acceptRejectApply(1);
            }
        });
        builder.show();
    }

    private void acceptRejectApply(final int flag) {
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteListener() {
            @Override
            public void onExecute() throws Exception {
                if (flag == 0) {
                    XMPPHelper.getInstance().accept(from);
                } else {
                    XMPPHelper.getInstance().refuse(from);
                }
            }

            @Override
            public void onFinish(Exception e) {

            }
        });
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


    private BroadcastReceiver applyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            notificationManager.cancel(CommonUtil.NOTICE_ID_TYPE_0);
            if (action.equals(CommonUtil.ACTION_RECEIVE_NOTICE)) {
                acceptRejectApply(0);
            }
        }
    };


    @Override
    public void onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(addFriendReceiver);
        unregisterReceiver(messageReceiver);
        unregisterReceiver(applyReceiver);
        super.onDestroy();
    }
}
