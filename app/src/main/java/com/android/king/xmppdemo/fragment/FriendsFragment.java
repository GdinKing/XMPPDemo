package com.android.king.xmppdemo.fragment;

import android.king.xmppdemo.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.king.xmppdemo.adapter.ChatAdapter;
import com.android.king.xmppdemo.adapter.UserAdapter;
import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.listener.OnNetworkExecuteListener;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;


/**
 * 通讯录
 */
public class FriendsFragment extends SupportFragment {

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    private ListView lvFriends;
    private TextView tvEmpty;
    private UserAdapter userAdapter;
    private List<User> dataList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        lvFriends = v.findViewById(R.id.lv_friends);
        tvEmpty = v.findViewById(R.id.tv_empty);

        initData();
        loadData();
        return v;
    }

    private void initData() {
        userAdapter = new UserAdapter(getActivity(), dataList);
        lvFriends.setAdapter(userAdapter);
        lvFriends.setEmptyView(tvEmpty);
    }

    private void loadData() {
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteListener() {
            @Override
            public void onExecute() throws Exception {
                dataList = XMPPHelper.getInstance().getAllFriends();
            }

            @Override
            public void onFinish(Exception e) {
                if (e != null) {
                    return;
                }
                userAdapter.refreshData(dataList);
            }
        });
    }

}
