package com.android.king.xmppdemo.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.king.albumpicker.AlbumPicker;
import com.android.king.albumpicker.util.AlbumConstant;
import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.adapter.MessageAdapter;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.db.SQLiteHelper;
import com.android.king.xmppdemo.entity.MessageBean;
import com.android.king.xmppdemo.event.ChatEvent;
import com.android.king.xmppdemo.event.ReadEvent;
import com.android.king.xmppdemo.event.SendMsgEvent;
import com.android.king.xmppdemo.fragment.PanelFragment;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.listener.OnTipDialogListener;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.util.GlideUtil;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SoftInputUtil;
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

/***
 * 聊天发消息界面
 * @since 2018-09-11
 * @author king
 */
public class MessageActivity extends BaseActivity implements AdapterView.OnItemLongClickListener,
        View.OnClickListener, EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener, PanelFragment.OnPanelItemClickListener {


    private static final int TYPE_EMOJI = 0;
    private static final int TYPE_ADD = 1;

    private boolean isEmojiShow = false;
    private boolean isAddShow = false;
    private ListView lvMessage;
    private TextView tvEmpty;
    private MessageAdapter messageAdapter;
    private List<MessageBean> dataList = new ArrayList<>();

    private EmojiconEditText etContent;
    private TextView tvSend;
    private ImageView ivAudio;
    private ImageView ivAdd;
    private ImageView ivEmoji;

    private String title;
    private String targetUser;
    private String msgDb;
    private int type;

    private View panelRoot;

    private Fragment addFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_message;
    }


    @Override
    protected void initView() {
        lvMessage = findViewById(R.id.lv_message);
        tvEmpty = findViewById(R.id.tv_empty);
        etContent = findViewById(R.id.et_content);
        tvSend = findViewById(R.id.tv_send);
        ivAdd = findViewById(R.id.iv_add);
        ivAudio = findViewById(R.id.iv_audio);
        ivEmoji = findViewById(R.id.iv_emoji);
        panelRoot = findViewById(R.id.panel_root);

        ivAdd.setOnClickListener(this);
        ivAudio.setOnClickListener(this);
        ivEmoji.setOnClickListener(this);
        tvSend.setOnClickListener(this);

        addFragment = PanelFragment.newInstance();

        etContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (panelRoot.isShown()) {
                        hidePanel(false);
                    }
                }
                return false;
            }
        });
        etContent.clearFocus();
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

        lvMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (panelRoot.isShown()) {
                    hidePanel(false);
                }
                return false;
            }
        });


        EventBus.getDefault().register(this);
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void initData() {
        targetUser = getIntent().getStringExtra("targetUser");
        title = getIntent().getStringExtra("title");
        msgDb = getIntent().getStringExtra("msgDb");
        type = getIntent().getIntExtra("type", AppConstants.ChatType.SINGLE);


        messageAdapter = new MessageAdapter(this, dataList);
        lvMessage.setAdapter(messageAdapter);
        lvMessage.setEmptyView(tvEmpty);
        lvMessage.setOnItemLongClickListener(this);

        setTitle(title);
        loadData();
    }


    private void loadData() {
        dataList.clear();
        AsyncExecutor.getInstance().execute(new OnExecuteCallback<Void>() {
            @Override
            public Void onExecute() throws Exception {
                Cursor cursor = SQLiteHelper.getMsgInstance(mContext, msgDb).rawQuery("select * from " + AppConstants.TABLE_MESSAGE+" LIMIT 15", null);
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
                    bean.setAvatar(XMPPHelper.getInstance().getVcardAvatar(from));
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
                resetUnreadCount(targetUser);
                messageAdapter.refreshData(dataList);
                scrollListViewToBottom();

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(AppConstants.MESSAGE_NOTIFY_ID);
            }
        });
    }

    private void scrollListViewToBottom() {
        if (lvMessage == null || messageAdapter == null) {
            return;
        }
        lvMessage.post(new Runnable() {
            @Override
            public void run() {
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
                if(isAddShow){
                    showPanel(TYPE_EMOJI);
                    return;
                }
                if (panelRoot.isShown()) {
                    hidePanel(true);
                } else {
                    showPanel(TYPE_EMOJI);
                }
                break;
            case R.id.iv_add:
                if(isEmojiShow){
                    showPanel(TYPE_ADD);
                    return;
                }
                if (panelRoot.isShown()) {
                    hidePanel(true);
                } else {
                    showPanel(TYPE_ADD);
                }
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

        AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                bean.setAvatar(XMPPHelper.getInstance().getVcardAvatar(getCurrentLogin()));
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
                dataList.add(bean);
                messageAdapter.refreshData(dataList);
                insertMsgDb(bean);
                scrollListViewToBottom();
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(AppConstants.MESSAGE_NOTIFY_ID);
                EventBus.getDefault().post(new SendMsgEvent(bean));
            }
        });
    }

    private void hidePanel(boolean showKeyBoard) {
        isEmojiShow = false;
        isAddShow = false;
        ivEmoji.setImageResource(R.drawable.ic_emoji);
        if (panelRoot.isShown()) {
            if (showKeyBoard) {
                panelRoot.setVisibility(View.GONE);
                showSoftInput(etContent);
            } else {
                panelRoot.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 显示底部工具栏
     *
     * @param type
     */
    private void showPanel(int type) {
        int panelHeight = SoftInputUtil.getKeyboardHeight(this);
        hideSoftInput();
        panelRoot.getLayoutParams().height = panelHeight;
        panelRoot.setVisibility(View.VISIBLE);
        if (type == TYPE_EMOJI) {//emoji表情
            showEmoji();
        } else if (type == TYPE_ADD) { //功能栏
            showAdd();
        }

    }

    /**
     * 显示emoji表情
     */
    private void showEmoji() {
        ivEmoji.setImageResource(R.drawable.ic_keyboard);
        getSupportFragmentManager().beginTransaction().replace(R.id.panel_root, EmojiconsFragment.newInstance(false)).commit();
        isEmojiShow = true;
        isAddShow = false;
    }

    /**
     * 显示功能栏
     */
    private void showAdd() {
        ivEmoji.setImageResource(R.drawable.ic_emoji);
        getSupportFragmentManager().beginTransaction().replace(R.id.panel_root, addFragment).commit();
        isAddShow = true;
        isEmojiShow = false;
    }

    /**
     * 接收到消息
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatEvent(ChatEvent event) {
        Logger.i("哈哈哈哈");
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
        SQLiteHelper.getMsgInstance(mContext, msgDb).insert(AppConstants.TABLE_MESSAGE, cv);

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
        EmojiconsFragment.input(etContent, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(etContent);
    }

    /**
     * 清空未读数
     *
     * @param from
     */
    private void resetUnreadCount(String from) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("unread", 0);
            SQLiteHelper.getInstance(this).update(AppConstants.TABLE_CHAT, cv, "fromUser=?", new String[]{from});
            EventBus.getDefault().post(new ReadEvent(targetUser));
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    @Override
    public void onPanelItemClick(int position) {
        switch (position) {
            case 0:
                //图片
                Intent intent = AlbumPicker.getInstance(this)
                        .setImageLoader(new GlideUtil())  //设置图片加载器
                        .setMax(9) //最大选择数
                        .setSelectType(AlbumConstant.TYPE_ALL)
                        .setMode(AlbumConstant.MODE_MULTI)
                        .getIntent();
                startActivityForResult(intent, 100);
                break;
            case 1:

                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 100 && data != null) {
            ArrayList<String> pathList = data.getStringArrayListExtra(AlbumConstant.RESULT_KEY_PATH_LIST);
            for (String path : pathList) {
               sendMsg(path);
            }
        }
    }
}
