package com.android.king.xmppdemo.fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.adapter.ChatAdapter;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.db.SQLiteHelper;
import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.entity.MessageBean;
import com.android.king.xmppdemo.event.ChatEvent;
import com.android.king.xmppdemo.event.MessageEvent;
import com.android.king.xmppdemo.event.SendMsgEvent;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.util.DisplayUtil;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SPUtil;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.SupportFragment;


/**
 * 会话列表界面
 */
public class ChatFragment extends SupportFragment implements AdapterView.OnItemClickListener {

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    private SwipeMenuListView lvChat;
    private TextView tvEmpty;
    private ChatAdapter chatAdapter;
    private List<ChatBean> dataList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        lvChat = v.findViewById(R.id.lv_chat);
        tvEmpty = v.findViewById(R.id.tv_empty);
        initData();
        loadData();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void initData() {
        //创建侧滑菜单按钮
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getActivity());
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                openItem.setWidth(DisplayUtil.dip2px(getActivity(), 90));
                openItem.setTitle("置顶");
                openItem.setTitleSize(18);
                openItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity());
                deleteItem.setBackground(new ColorDrawable(Color.RED));
                deleteItem.setWidth(DisplayUtil.dip2px(getActivity(), 90));
                deleteItem.setIcon(R.drawable.ic_delete);
                menu.addMenuItem(deleteItem);
            }
        };
        lvChat.setMenuCreator(creator);

        chatAdapter = new ChatAdapter(getActivity(), dataList);
        lvChat.setAdapter(chatAdapter);
        lvChat.setEmptyView(tvEmpty);
        lvChat.setOnItemClickListener(this);

        lvChat.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                ChatBean bean = dataList.get(position);
                switch (index) {
                    case 0:
                        Toast.makeText(getActivity(), "置顶" + bean.getTarget(), Toast.LENGTH_SHORT).show();
                        dataList.add(0, dataList.get(position));
                        dataList.remove(position + 1);

                        break;
                    case 1:
                        Toast.makeText(getActivity(), "删除" + bean.getTarget(), Toast.LENGTH_SHORT).show();
                        SQLiteHelper.getInstance(getActivity()).delete(AppConstants.TABLE_CHAT, "fromUser=?", new String[]{bean.getTarget()});
                        getActivity().deleteDatabase(bean.getMsgDb());
                        dataList.remove(position);
                        break;
                }
                chatAdapter.refreshData(dataList);
                ((HomeFragment) getParentFragment()).setMessageBadge(chatAdapter.getTotalUnread());
                return false;
            }
        });
    }

    private void loadData() {
        dataList.clear();
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback<Void>() {
            @Override
            public Void onExecute() throws Exception {
                Cursor cursor = SQLiteHelper.getInstance(getActivity()).rawQuery("select * from " + AppConstants.TABLE_CHAT, null);
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    int unreadCount = cursor.getInt(cursor.getColumnIndex("unread"));
                    int type = cursor.getInt(cursor.getColumnIndex("type"));
                    String from = cursor.getString(cursor.getColumnIndex("fromUser"));
                    String message = cursor.getString(cursor.getColumnIndex("message"));
                    String msgDb = cursor.getString(cursor.getColumnIndex("msgDb"));
                    long time = cursor.getLong(cursor.getColumnIndex("time"));
                    Logger.i("目标：" + from);
                    ChatBean bean = new ChatBean();
                    bean.setId(id);
                    bean.setType(type);
                    bean.setMessage(message);
                    bean.setTime(time);
                    bean.setTarget(from);
                    bean.setMsgDb(msgDb);
                    bean.setUnreadCount(unreadCount);
                    dataList.add(bean);
                }
                return null;
            }

            @Override
            public void onFinish(Void result, Exception e) {
                if (e != null) {
                    Logger.e(e);
                    return;
                }
                chatAdapter.refreshData(dataList);
                ((HomeFragment) getParentFragment()).setMessageBadge(chatAdapter.getTotalUnread());
            }
        });
    }


    /**
     * 接收到消息
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        ChatBean chatBean = event.chatBean;
        String target = chatBean.getTarget();
        String message = chatBean.getMessage();
        long time = chatBean.getTime();
        int index = chatAdapter.isExist(target);
        if (index < 0) {
            chatBean.setUnreadCount(1);
            dataList.add(chatBean);
            insertChatDb(chatBean);
        } else {
            dataList.get(index).setMessage(message);
            dataList.get(index).setTime(time);
            dataList.get(index).setUnreadCount(dataList.get(index).getUnreadCount() + 1);
            updateChatDb(chatBean);

        }
        insertMsgDb(chatBean);
        chatAdapter.refreshData(dataList);
        ((HomeFragment) getParentFragment()).setMessageBadge(chatAdapter.getTotalUnread());
    }

    /**
     * 发送消息后的响应
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSendMsgEvent(SendMsgEvent event) {
        MessageBean message = event.message;
        String to = message.getTo();
        String content = message.getContent();
        long time = message.getTime();

        ChatBean chatBean = new ChatBean();
        chatBean.setTarget(to);
        chatBean.setTime(time);
        chatBean.setMessage(content);
        chatBean.setMsgDb(message.getMsgDb());
        int index = chatAdapter.isExist(to);
        if (index < 0) {
            dataList.add(chatBean);
            insertChatDb(chatBean);
        } else {
            dataList.get(index).setMessage(content);
            dataList.get(index).setTime(time);
            updateChatDb(chatBean);
        }
        chatAdapter.refreshData(dataList);
    }


    private void insertChatDb(final ChatBean chatBean) {
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                String from = chatBean.getTarget();
                String message = chatBean.getMessage();
                long time = chatBean.getTime();
                ContentValues cv = new ContentValues();
                cv.put("fromUser", from);
                cv.put("message", message);
                cv.put("msgDb", chatBean.getMsgDb());
                cv.put("type", chatBean.getType());
                cv.put("unread", 1);
                cv.put("time", time);
                SQLiteHelper.getInstance(getActivity()).insert(AppConstants.TABLE_CHAT, cv);
                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {
                if (e != null) {
                    Logger.e(e);
                    return;
                }
            }
        });
    }

    /**
     * 插入消息表
     *
     * @param chatBean
     */
    private void insertMsgDb(final ChatBean chatBean) {
        final String current = SPUtil.getString(getActivity(), AppConstants.SP_KEY_LOGIN_ACCOUNT);
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                String from = chatBean.getTarget();
                String message = chatBean.getMessage();
                long time = chatBean.getTime();
                String msgDb = chatBean.getMsgDb();
                ContentValues cv = new ContentValues();
                cv.put("fromUser", from);
                cv.put("toUser", current);
                cv.put("content", message);
                cv.put("type", AppConstants.ChatType.SINGLE);
                cv.put("time", time);
                cv.put("status", AppConstants.MessageStatus.SUCCESS);
                cv.put("category", AppConstants.MessageType.IN_TEXT);
                SQLiteHelper.getMsgInstance(getActivity(), msgDb).insert(AppConstants.TABLE_MESSAGE, cv);
                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {
                if (e != null) {
                    Logger.e(e);
                    return;
                }
                EventBus.getDefault().post(new ChatEvent());
            }
        });
    }

    private void updateChatDb(final ChatBean chatBean) {
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {

                String from = chatBean.getTarget();
                String message = chatBean.getMessage();
                long time = chatBean.getTime();

                int unreadCount = 0;
                Cursor cursor = SQLiteHelper.getInstance(getActivity()).rawQuery("select unread from " + AppConstants.TABLE_CHAT + " where fromUser=?", new String[]{from});
                if (cursor.moveToFirst()) {
                    unreadCount = cursor.getInt(cursor.getColumnIndex("unread"));
                }
                unreadCount += 1;
                ContentValues cv = new ContentValues();
                cv.put("message", message);
                cv.put("unread", unreadCount);
                cv.put("time", time);
                SQLiteHelper.getInstance(getActivity()).update(AppConstants.TABLE_CHAT, cv, "fromUser=?", new String[]{from});

                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {
                if (e != null) {
                    Logger.e(e);
                    return;
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ChatBean bean = dataList.get(position);
        resetUnreadCount(bean.getTarget());
        ((HomeFragment) getParentFragment()).startFragment(MessageFragment.newInstance(bean.getTarget(), bean.getMsgDb(), bean.getType()));
    }

    /**
     * 清除未读数
     *
     * @param from
     */
    private void resetUnreadCount(String from) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("unread", 0);
            SQLiteHelper.getInstance(getActivity()).update(AppConstants.TABLE_CHAT, cv, "fromUser=?", new String[]{from});
            int index = chatAdapter.isExist(from);
            dataList.get(index).setUnreadCount(0);
            chatAdapter.refreshData(dataList);
            ((HomeFragment) getParentFragment()).setMessageBadge(chatAdapter.getTotalUnread());
        } catch (Exception e) {
            Logger.e(e);
        }
    }
}
