package com.android.king.xmppdemo.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.king.xmppdemo.adapter.ChatAdapter;
import com.android.king.xmppdemo.entity.ChatBean;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.king.xmppdemo.R;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;


/**
 *
 */
public class ChatFragment extends SupportFragment {

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    private ListView lvChat;
    private TextView tvEmpty;
    private ChatAdapter chatAdapter;
    private List<ChatBean> dataList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        lvChat = v.findViewById(R.id.lv_chat);
        tvEmpty = v.findViewById(R.id.tv_empty);

        loadData();
        return v;
    }


    private void loadData() {
        if (chatAdapter == null) {
            chatAdapter = new ChatAdapter(getActivity(), dataList);
            lvChat.setAdapter(chatAdapter);
            lvChat.setEmptyView(tvEmpty);
        } else {
            chatAdapter.refreshData(dataList);
        }
    }


    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatBean bean = new ChatBean();
            bean.setMessage("哈哈哈");
            bean.setUser("李四");


            mHandler.obtainMessage(100, bean).sendToTarget();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                ChatBean bean = (ChatBean) msg.obj;
                //TODO 写入数据库

                if (chatAdapter.isExist(bean.getUser())) {
                    chatAdapter.refreshData(dataList);
                }
            }
        }
    };
}
