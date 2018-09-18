package com.android.king.xmppdemo.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.adapter.FriendAdapter;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.db.SQLiteHelper;
import com.android.king.xmppdemo.entity.Apply;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.event.AgreeEvent;
import com.android.king.xmppdemo.event.LoginEvent;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;
import q.rorbin.badgeview.QBadgeView;


/**
 * 通讯录
 */
public class FriendsFragment extends SupportFragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    private ListView lvFriends;
    private TextView tvEmpty;
    private FriendAdapter mAdapter;
    private List<User> dataList = new ArrayList<>();

    private RelativeLayout rlNewFriend;
    private RelativeLayout rlMultiChat;

    private int badgeCount = 0;
    private QBadgeView badgeView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        lvFriends = v.findViewById(R.id.lv_friends);
        tvEmpty = v.findViewById(R.id.tv_empty);
        rlNewFriend = v.findViewById(R.id.rl_new_friend);
        rlMultiChat = v.findViewById(R.id.rl_multi_chat);
        rlMultiChat.setOnClickListener(this);
        rlNewFriend.setOnClickListener(this);
        lvFriends.setOnItemClickListener(this);
        initData();
        loadData();
        return v;
    }


    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

    private void initData() {
        mAdapter = new FriendAdapter(getActivity(), dataList);
        lvFriends.setAdapter(mAdapter);
        lvFriends.setEmptyView(tvEmpty);

    }

    public void loadData() {
        AsyncExecutor.getInstance().execute(new OnExecuteCallback<List<User>>() {
            @Override
            public List<User> onExecute() throws Exception {
                List<User> userList = XMPPHelper.getInstance().getAllFriends();
                return userList;
            }

            @Override
            public void onFinish(List<User> result, Exception e) {
                if (e != null) {
                    return;
                }
                dataList = result;
                mAdapter.refreshData(dataList);
                checkApply();
            }

        });
    }


    public void checkApply() {
        hideBadge();
        try {
            Cursor cursor = SQLiteHelper.getInstance(getActivity()).rawQuery("select * from " + AppConstants.TABLE_APPLY, null);
            if (null != cursor) {
                while (cursor.moveToNext()) {
                    int isAgree = cursor.getInt(cursor.getColumnIndex("isAgree"));
                    if (isAgree == Apply.STATUS_UNAGREE) {//有未同意的
                        addFriendBadge();
                    }
                }

            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginEvent event) {
        if (event.isLogin) {
            loadData();
        }
    }

    public void addFriendBadge() {
        badgeCount += 1;
        badgeView = new QBadgeView(getActivity());
        badgeView.bindTarget(rlNewFriend)
                .setBadgeTextSize(14.0f, true)
                .setBadgeGravity(Gravity.CENTER | Gravity.END)
                .setGravityOffset(10.0f, true)
                .setBadgePadding(8.0f, true)
                .setBadgeNumber(badgeCount);
        ((HomeFragment) getParentFragment()).setFriendBadge(badgeCount);
    }


    public void hideBadge() {
        if (badgeView != null) {
            badgeCount = 0;
            badgeView.hide(false);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_new_friend:
                ((HomeFragment) getParentFragment()).startFragment(NewApplyFragment.newInstance());
                break;
            case R.id.rl_multi_chat:

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        User user = dataList.get(position);
        ((HomeFragment) getParentFragment()).startFragment(UserFragment.newInstance(user.getAccount(), user.getNote()));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AgreeEvent event) {
        checkApply();
    }
}
