package com.android.king.xmppdemo.fragment;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.adapter.ChatAdapter;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.db.SQLiteHelper;
import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.entity.MessageBean;
import com.android.king.xmppdemo.event.ChatEvent;
import com.android.king.xmppdemo.event.InviteEvent;
import com.android.king.xmppdemo.event.MessageEvent;
import com.android.king.xmppdemo.event.ReadEvent;
import com.android.king.xmppdemo.event.SendMsgEvent;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.listener.OnTipDialogListener;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.ui.MessageActivity;
import com.android.king.xmppdemo.util.CommonUtil;
import com.android.king.xmppdemo.util.DisplayUtil;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SPUtil;
import com.android.king.xmppdemo.xmpp.XMPPHelper;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.stringencoder.Base64;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * 会话列表界面
 */
public class ChatFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    private SwipeMenuListView lvChat;
    private TextView tvEmpty;
    private ChatAdapter chatAdapter;
    private List<ChatBean> dataList = new ArrayList<>();
    private List<Message> offlineMessage = new ArrayList<>();


    @Override
    protected int getContentView() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void initView() {
        lvChat = rootView.findViewById(R.id.lv_chat);
        tvEmpty = rootView.findViewById(R.id.tv_empty);

        EventBus.getDefault().register(this);

    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void initData() {
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
                switch (index) {
                    case 0:
                        ChatBean bean = (ChatBean) chatAdapter.getItem(position);
                        bean.setLevel(0);
                        updateChatDb(bean);
                        loadData();
                        break;
                    case 1:
                        deleteTip(position);
                        break;
                }
                return false;
            }
        });

        loadData();
//        getOfflineMessage();  因为我的openfire装了一个离线消息的插件，所以这里不需要获取离线消息，登录后插件会自动发送离线消息给客户端
    }


    private void deleteTip(final int position) {
        showTip("是否删除该聊天？", new OnTipDialogListener() {
            @Override
            public void onPositiveClick() {
                ChatBean bean = dataList.get(position);
                SQLiteHelper.getInstance(getActivity()).delete(AppConstants.TABLE_CHAT, "fromUser=?", new String[]{bean.getTarget()});
                SQLiteHelper.getInstance(getActivity()).deleteDatabase(getActivity(), bean.getMsgDb());
                dataList.remove(position);
                chatAdapter.refreshData(dataList);
                ((HomeFragment) getParentFragment()).setMessageBadge(chatAdapter.getTotalUnread());
            }

            @Override
            public void onNegativeClick() {

            }
        });
    }

    /**
     * 获取离线消息
     */
    private void getOfflineMessage() {
        AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                offlineMessage = XMPPHelper.getInstance().getOfflineMessage();
                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {

                if (e != null) {
                    XMPPHelper.getInstance().changeStatus(AppConstants.StanzaStatus.AVAILABLE);
                    Logger.e(e);
                    return;
                }
                for (Message message : offlineMessage) {
                    ChatBean bean = new ChatBean();
                    bean.setTitle(message.getFrom().toString().split("@")[0]);
                    bean.setTarget(message.getFrom().toString());
                    bean.setTime(System.currentTimeMillis());
                    bean.setMessage(message.getBody());
                    bean.setType(AppConstants.ChatType.SINGLE);
                    bean.setMsgDb(Base64.encode(message.getFrom().toString().split("@")[0]));
                    bean.setLevel(2);
                    EventBus.getDefault().post(new MessageEvent(bean));
                }
            }
        });
    }

    private void loadData() {
        dataList.clear();
        AsyncExecutor.getInstance().execute(new OnExecuteCallback<Void>() {
            @Override
            public Void onExecute() throws Exception {
                Cursor cursor = SQLiteHelper.getInstance(getActivity()).rawQuery("select * from " + AppConstants.TABLE_CHAT+" order by level asc", null);
                while (cursor.moveToNext()) {
                    ChatBean bean = new ChatBean();
                    String fromUser = cursor.getString(cursor.getColumnIndex("fromUser"));
                    bean.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    bean.setType(cursor.getInt(cursor.getColumnIndex("type")));
                    bean.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                    bean.setMessage(cursor.getString(cursor.getColumnIndex("message")));
                    bean.setTime(cursor.getLong(cursor.getColumnIndex("time")));
                    bean.setTarget(fromUser);
                    bean.setMsgDb(cursor.getString(cursor.getColumnIndex("msgDb")));
                    bean.setUnreadCount(cursor.getInt(cursor.getColumnIndex("unread")));
                    bean.setLevel(cursor.getInt(cursor.getColumnIndex("level")));
                    bean.setAvatar(XMPPHelper.getInstance().getVcardAvatar(fromUser));

                    Logger.i("头像："+bean.getAvatar());
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


        Intent intent = new Intent(getActivity(), MessageActivity.class);
        intent.putExtra("targetUser", target);
        intent.putExtra("msgDb", chatBean.getMsgDb());
        intent.putExtra("type", chatBean.getType());
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 100, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        CommonUtil.showMsgNotify(getActivity(), chatBean.getTitle(), message, pendingIntent);

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
        chatBean.setTitle(to.split("@")[0]);
        chatBean.setTarget(to);
        chatBean.setTime(time);
        chatBean.setMessage(content);
        chatBean.setUnreadCount(0);
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

    /**
     * 插入聊天表
     * @param chatBean
     */
    private void insertChatDb(final ChatBean chatBean) {
        AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                String from = chatBean.getTarget();
                String message = chatBean.getMessage();
                long time = chatBean.getTime();
                ContentValues cv = new ContentValues();
                cv.put("title", chatBean.getTitle());
                cv.put("fromUser", from);
                cv.put("message", message);
                cv.put("msgDb", chatBean.getMsgDb());
                cv.put("type", chatBean.getType());
                cv.put("unread", 1);
                cv.put("level", chatBean.getLevel());
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
        AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                String msgDb = chatBean.getMsgDb();
                ContentValues cv = new ContentValues();
                cv.put("fromUser", chatBean.getTarget());
                cv.put("toUser", current);
                cv.put("content", chatBean.getMessage());
                cv.put("type", AppConstants.ChatType.SINGLE);
                cv.put("time", chatBean.getTime());
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
                Logger.i("更新消息");
                EventBus.getDefault().post(new ChatEvent());
            }
        });
    }

    private void updateChatDb(final ChatBean chatBean) {
        AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {

                String from = chatBean.getTarget();
                ContentValues cv = new ContentValues();
                cv.put("message", chatBean.getMessage());
                cv.put("unread", chatBean.getUnreadCount());
                cv.put("time", chatBean.getTime());
                cv.put("level", chatBean.getLevel());
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
        if (AppConstants.ChatType.MULTI_INVITE == bean.getType()) {
            //群聊邀请
            showToast("群聊");
        } else {
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            intent.putExtra("targetUser", bean.getTarget());
            intent.putExtra("msgDb", bean.getMsgDb());
            intent.putExtra("type", bean.getType());
            intent.putExtra("title", bean.getTitle());
            startActivity(intent);
        }

    }

    /**
     * 清除未读数
     *
     * @param from
     */
    private void resetUnreadCount(String from) {
        try {
            int index = chatAdapter.isExist(from);
            if (index < 0) {
                return;
            }
            dataList.get(index).setUnreadCount(0);
            chatAdapter.refreshData(dataList);
            ((HomeFragment) getParentFragment()).setMessageBadge(chatAdapter.getTotalUnread());
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadEvent(ReadEvent e) {
        resetUnreadCount(e.from);
    }


    /**
     * 群聊邀请响应
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInviteEvent(InviteEvent event) {
        ChatBean chatBean = event.chatBean;
        String to = chatBean.getTarget();
        String content = chatBean.getMessage();
        long time = chatBean.getTime();
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

}
