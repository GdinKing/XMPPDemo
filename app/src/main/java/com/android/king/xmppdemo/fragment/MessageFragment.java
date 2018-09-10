package com.android.king.xmppdemo.fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.adapter.MessageAdapter;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.db.SQLiteHelper;
import com.android.king.xmppdemo.entity.MessageBean;
import com.android.king.xmppdemo.event.ChatEvent;
import com.android.king.xmppdemo.event.SendMsgEvent;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.listener.OnTipDialogListener;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.List;

import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconGridFragment;
import io.github.rockerhieu.emojicon.EmojiconsFragment;
import io.github.rockerhieu.emojicon.emoji.Emojicon;


/**
 * 聊天界面
 */
public class MessageFragment extends BaseFragment implements AdapterView.OnItemLongClickListener, View.OnClickListener, EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    /**
     * @param target 对方的账号
     * @param msgDb  缓存聊天记录的数据库名
     * @param type   单聊/群聊
     * @return
     */
    public static MessageFragment newInstance(String target, String msgDb, int type) {
        MessageFragment fragment = new MessageFragment();

        Bundle b = new Bundle();
        b.putString("targetUser", target);
        b.putString("msgDb", msgDb);
        b.putInt("type", type);
        fragment.setArguments(b);
        return fragment;
    }

    private ListView lvMessage;
    private TextView tvEmpty;
    private MessageAdapter messageAdapter;
    private List<MessageBean> dataList = new ArrayList<>();

    private EmojiconEditText etContent;
    private TextView tvSend;
    private ImageView ivAudio;
    private ImageView ivAdd;
    private ImageView ivEmoji;

    private String targetUser;
    private String msgDb;
    private int type;


    private Fragment emojiFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        targetUser = getArguments().getString("targetUser");
        msgDb = getArguments().getString("msgDb");
        type = getArguments().getInt("type");
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_message;
    }

    @Override
    protected void initView() {
        lvMessage = rootView.findViewById(R.id.lv_message);
        tvEmpty = rootView.findViewById(R.id.tv_empty);
        etContent = rootView.findViewById(R.id.et_content);
        tvSend = rootView.findViewById(R.id.tv_send);
        ivAdd = rootView.findViewById(R.id.iv_add);
        ivAudio = rootView.findViewById(R.id.iv_audio);
        ivEmoji = rootView.findViewById(R.id.iv_emoji);

        emojiFragment = getChildFragmentManager().findFragmentById(R.id.emojicons);
        closeEmoji();
        ivAdd.setOnClickListener(this);
        ivAudio.setOnClickListener(this);
        ivEmoji.setOnClickListener(this);
        tvSend.setOnClickListener(this);
        etContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    closeEmoji();
                }
                return false;
            }
        });
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    ivAdd.setVisibility(View.GONE);
                    tvSend.setVisibility(View.VISIBLE);
                } else {
                    ivAdd.setVisibility(View.VISIBLE);
                    tvSend.setVisibility(View.GONE);
                }
            }
        });
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

    @Override
    protected void initData() {
        messageAdapter = new MessageAdapter(getActivity(), dataList);
        lvMessage.setAdapter(messageAdapter);
        lvMessage.setEmptyView(tvEmpty);
        lvMessage.setOnItemLongClickListener(this);

        setTitle(targetUser.split("@")[0]);
        loadData();
    }


    private void loadData() {
        dataList.clear();
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback<Void>() {
            @Override
            public Void onExecute() throws Exception {
                Cursor cursor = SQLiteHelper.getMsgInstance(getActivity(), msgDb).rawQuery("select * from " + AppConstants.TABLE_MESSAGE, null);
                while (cursor.moveToNext()) {
                    int category = cursor.getInt(cursor.getColumnIndex("category"));
                    int type = cursor.getInt(cursor.getColumnIndex("type"));
                    String from = cursor.getString(cursor.getColumnIndex("fromUser"));
                    String to = cursor.getString(cursor.getColumnIndex("toUser"));
                    String content = cursor.getString(cursor.getColumnIndex("content"));
                    long time = cursor.getLong(cursor.getColumnIndex("time"));

                    MessageBean bean = new MessageBean();
                    bean.setType(type);
                    bean.setContent(content);
                    bean.setTime(time);
                    bean.setFrom(from);
                    bean.setTo(to);
                    bean.setCategory(category);
                    bean.setMsgDb(msgDb);
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
                messageAdapter.refreshData(dataList);
                lvMessage.setSelection(messageAdapter.getCount() - 1);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.tv_send:
                String content = etContent.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                sendMsg(content);
                etContent.setText("");
                break;
            case R.id.iv_emoji:
                openEmoji();
                break;
            case R.id.iv_add:

                break;
            case R.id.iv_audio:

                break;
        }
    }

    /**
     * 发消息
     *
     * @param msg
     */
    private void sendMsg(final String msg) {
        final MessageBean bean = new MessageBean();
        bean.setContent(msg);
        bean.setTime(System.currentTimeMillis());
        bean.setCategory(AppConstants.MessageType.OUT_TEXT);
        bean.setFrom(getCurrentLogin());
        bean.setMsgDb(msgDb);
        bean.setType(type);
        bean.setTo(targetUser);

        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                XMPPHelper.getInstance().sendUserMsg(Message.Type.chat, "", targetUser, msg);
                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {
                if (e != null) {
                    showToast("发送失败");
                    bean.setStatus(AppConstants.MessageStatus.ERROR);
                } else {
                    bean.setStatus(AppConstants.MessageStatus.SUCCESS);
                }
                insertMsgDb(bean);
                dataList.add(bean);
                messageAdapter.refreshData(dataList);
                lvMessage.setSelection(messageAdapter.getCount() - 1);
                EventBus.getDefault().post(new SendMsgEvent(bean));
            }
        });
    }

    private void openEmoji() {
        if (emojiFragment.isAdded() && emojiFragment.isHidden()) {
            hideSoftInput();
            getChildFragmentManager().beginTransaction().show(emojiFragment).commit();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    lvMessage.setSelection(messageAdapter.getCount() - 1);
                }
            },500);
        }
    }

    private void closeEmoji() {
        if (emojiFragment.isAdded() && !emojiFragment.isHidden()) {
            getChildFragmentManager().beginTransaction().hide(emojiFragment).commit();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    lvMessage.setSelection(messageAdapter.getCount() - 1);
                }
            },500);
        }
    }

    /**
     * 接收到消息
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatEvent(ChatEvent event) {
        loadData();
    }

    /**
     * 插入消息到数据库
     *
     * @param bean
     */
    private void insertMsgDb(final MessageBean bean) {
        ContentValues cv = new ContentValues();
        cv.put("fromUser", bean.getFrom());
        cv.put("toUser", bean.getTo());
        cv.put("content", bean.getContent());
        cv.put("type", bean.getType());
        cv.put("category", bean.getCategory());
        cv.put("status", bean.getStatus());
        cv.put("time", bean.getTime());
        SQLiteHelper.getMsgInstance(getActivity(), msgDb).insert(AppConstants.TABLE_MESSAGE, cv);

    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        MessageBean bean = dataList.get(position);
        showTip("是否删除该消息？", new OnTipDialogListener() {
            @Override
            public void onPositiveClick() {
                dataList.remove(position);
            }

            @Override
            public void onNegativeClick() {

            }
        });
        return true;
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {

        hideSoftInput();
        EmojiconsFragment.input(etContent, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {

        hideSoftInput();
        EmojiconsFragment.backspace(etContent);
    }

    @Override
    public boolean onBackPressedSupport() {
        if (emojiFragment.isAdded() && !emojiFragment.isHidden()) {
            closeEmoji();
        } else {
            pop();
        }
        return true;
    }
}
