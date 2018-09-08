package com.android.king.xmppdemo.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.db.SQLiteHelper;
import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.ui.LoginActivity;
import com.android.king.xmppdemo.util.CommonUtil;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import me.yokeyword.fragmentation.SupportFragment;
import q.rorbin.badgeview.QBadgeView;


/**
 * 微聊
 */
public class HomeFragment extends SupportFragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    private SupportFragment[] mFragments = new SupportFragment[4];

    private RadioGroup rgBottom;
    private TextView tvTitle;

    private ImageView ivSearch;
    private ImageView ivAdd;

    private PopupWindow popupWindow;

    private int prePosition = 0;

    private View rootView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SupportFragment firstFragment = findChildFragment(MessageFragment.class);
        if (firstFragment == null) {
            mFragments[0] = MessageFragment.newInstance();
            mFragments[1] = FriendsFragment.newInstance();
            mFragments[2] = FindFragment.newInstance();
            mFragments[3] = MyFragment.newInstance();
            loadMultipleRootFragment(R.id.home_container, 0, mFragments[0], mFragments[1], mFragments[2], mFragments[3]);
        } else {
            mFragments[0] = firstFragment;
            mFragments[1] = findChildFragment(FriendsFragment.class);
            mFragments[2] = findChildFragment(FindFragment.class);
            mFragments[3] = findChildFragment(MyFragment.class);
        }
        prePosition = 0;

        IntentFilter filter = new IntentFilter(AppConstants.ACTION_FRIEND);
        getActivity().registerReceiver(addFriendReceiver, filter);

        IntentFilter filter1 = new IntentFilter(AppConstants.ACTION_RECONNECT_ERROR);
        getActivity().registerReceiver(reconnectReceiver, filter1);


        IntentFilter msgFilter = new IntentFilter(AppConstants.ACTION_INCOME_MESSAGE);
        getActivity().registerReceiver(messageReceiver, msgFilter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        tvTitle = rootView.findViewById(R.id.tv_title);
        ivAdd = rootView.findViewById(R.id.iv_add);
        ivSearch = rootView.findViewById(R.id.iv_search);
        ivAdd.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        rgBottom = rootView.findViewById(R.id.rg_bottom);
        rgBottom.setOnCheckedChangeListener(this);
        tvTitle.setText("微聊");
        return rootView;
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_chat:
                showHideFragment(mFragments[0], mFragments[prePosition]);
                prePosition = 0;
                break;
            case R.id.rb_friends:
                showHideFragment(mFragments[1], mFragments[prePosition]);
                prePosition = 1;
                break;
            case R.id.rb_moment:
                showHideFragment(mFragments[2], mFragments[prePosition]);
                prePosition = 2;
                break;
            case R.id.rb_my:
                showHideFragment(mFragments[3], mFragments[prePosition]);
                ((MyFragment) mFragments[3]).loadData();
                prePosition = 3;
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add:
                showAddMenu();
                break;
            case R.id.iv_search:

                break;

        }
    }

    /**
     * 添加好友监听
     */
    private BroadcastReceiver addFriendReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int flag = intent.getIntExtra(AppConstants.INTENT_KEY_ADD_FRIEND, -1);
            String from = intent.getStringExtra(AppConstants.INTENT_KEY_ADD_FRIEND_FROM);
            if (flag == AppConstants.STATUS_ADD_FRIEND_OK) {
                Toast.makeText(getActivity(), from.split("@")[0] + "通过了你的好友申请！", Toast.LENGTH_LONG).show();
                ((FriendsFragment) mFragments[1]).loadData();
            } else if (flag == AppConstants.STATUS_ADD_FRIEND_RECEIVE) {
                CommonUtil.showNotify(getActivity(), from.split("@")[0] + "请求加你为好友");

                mHandler.obtainMessage(100, from).sendToTarget();

            } else if (flag == AppConstants.STATUS_ADD_FRIEND_REJECJ) {
                Toast.makeText(getActivity(), from.split("@")[0] + "拒绝了你的好友申请！", Toast.LENGTH_LONG).show();
            }
        }
    };

    private QBadgeView friendBadge;
    private QBadgeView messageBadge;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 100:
                    String from = (String) msg.obj;
                    checkApplyExist(from);
                    ((FriendsFragment) mFragments[1]).checkApply();
                    int count = ((FriendsFragment) mFragments[1]).getBadgeCount();
                    if (count > 0) {
                        friendBadge = new QBadgeView(getActivity());
                        friendBadge.bindTarget(rootView.findViewById(R.id.rb_friends)).setBadgeNumber(count);
                    } else {
                        if (friendBadge != null) {
                            friendBadge.hide(false);
                        }
                    }
                    break;


            }

        }
    };

    /**
     * 查询好友申请是否已存在数据库表中，存在则置isAgree为0，不存在就插入
     *
     * @param from
     */
    private void checkApplyExist(String from) {
        Cursor cursor = SQLiteHelper.getInstance(getActivity()).query(AppConstants.TABLE_APPLY, new String[]{"fromUser", "isAgree"}, "fromUser=?", new String[]{from}, null, null, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                ContentValues cv = new ContentValues();
                cv.put("isAgree", 0);
                SQLiteHelper.getInstance(getActivity()).update(AppConstants.TABLE_APPLY, cv, "fromUser=?", new String[]{from});
            } else {
                ContentValues cv = new ContentValues();
                cv.put("fromUser", from);
                cv.put("name", from.split("@")[0]);
                cv.put("isAgree", 0);
                SQLiteHelper.getInstance(getActivity()).insert(AppConstants.TABLE_APPLY, cv);

            }
        }
    }

    /**
     * 重新连接失败监听
     */
    private BroadcastReceiver reconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            XMPPHelper.getInstance().logout();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    };

    /**
     * 弹出请求添加好友对话框
     */
//    private void showApplyDialog(final String from) {
//
//        final TipDialog dialog = new TipDialog(getActivity());
//        dialog.setNegativeText("拒绝");
//        dialog.setPositiveText("接受");
//        dialog.setTipMessage(from.split("@")[0] + "请求加你为好友");
//        dialog.setOnTipClickListener(new TipDialog.OnTipClickListener() {
//            @Override
//            public void onPositiveClick() {
//                dialog.dismiss();
//                acceptRejectApply(from, 0);
//
//            }
//
//            @Override
//            public void onNegativeClick() {
//                dialog.dismiss();
//                acceptRejectApply(from, 1);
//            }
//        });
//        dialog.show();
//
//    }


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
//            Toast.makeText(getActivity(), bean.getUser() + ":" + bean.getMessage(), Toast.LENGTH_LONG).show();
        }
    };


    private void showAddMenu() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popup_layout_add_menu, null);
        TextView tvAddFrient = view.findViewById(R.id.tv_add_friend);
        TextView tvMultiChat = view.findViewById(R.id.tv_multi_chat);
        TextView tvScan = view.findViewById(R.id.tv_scan);

        tvAddFrient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                start(AddFriendFragment.newInstance());
            }
        });

        tvMultiChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        tvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        if (popupWindow == null) {
            popupWindow = new PopupWindow(view);
            popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
        }
        popupWindow.showAsDropDown(ivAdd, 0, 10);
    }

    @Override
    public boolean onBackPressedSupport() {
        getActivity().moveTaskToBack(true);
        return true;
    }

    public void startFragment(SupportFragment fragment) {
        start(fragment);
    }

    @Override
    public void onDetach() {
        getActivity().unregisterReceiver(addFriendReceiver);
        getActivity().unregisterReceiver(reconnectReceiver);
        getActivity().unregisterReceiver(messageReceiver);
        super.onDetach();
    }

}
