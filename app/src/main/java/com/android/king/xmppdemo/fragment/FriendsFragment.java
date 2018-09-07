package com.android.king.xmppdemo.fragment;

import android.king.xmppdemo.R;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.king.xmppdemo.adapter.FriendAdapter;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

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

    private void initData() {
        mAdapter = new FriendAdapter(getActivity(), dataList);
        lvFriends.setAdapter(mAdapter);
        lvFriends.setEmptyView(tvEmpty);

    }

    public void loadData() {
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback() {
            @Override
            public void onExecute() throws Exception {
                dataList = XMPPHelper.getInstance().getAllFriends();
            }

            @Override
            public void onFinish(Exception e) {
                if (e != null) {
                    return;
                }
                mAdapter.refreshData(dataList);
            }
        });
    }

    private int badgeCount = 0;

    private QBadgeView badgeView;

    public void addFriendBadge() {
        badgeCount += 1;
        badgeView = new QBadgeView(getActivity());
        badgeView.bindTarget(rlNewFriend)
                .setBadgeTextSize(14.0f, true)
                .setBadgeGravity(Gravity.CENTER | Gravity.END)
                .setGravityOffset(10.0f, true)
                .setBadgePadding(8.0f, true)
                .setBadgeNumber(badgeCount);
    }

    public void hideBadge() {
        if (badgeView != null) {
            badgeView.hide(false);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_new_friend:

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
}
