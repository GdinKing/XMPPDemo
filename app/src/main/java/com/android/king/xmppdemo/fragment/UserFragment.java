package com.android.king.xmppdemo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.ui.MessageActivity;
import com.android.king.xmppdemo.util.ImageUtil;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import org.jivesoftware.smack.SmackException;

import java.util.List;


/**
 *
 */
public class UserFragment extends BaseFragment implements View.OnClickListener {

    public static UserFragment newInstance(String account, String note) {
        UserFragment fragment = new UserFragment();
        Bundle b = new Bundle();
        b.putString("account", account);
        b.putString("note", note);
        fragment.setArguments(b);
        return fragment;
    }

    private TextView btnNote;
    private TextView btnSend;
    private TextView btnAddFriend;
    private TextView tvAccount;
    private TextView tvNick;
    private TextView tvNote; //备注名
    private ImageView ivAvatar;

    private String account;
    private String note;

    private User user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        account = args.getString("account");
        note = args.getString("note");
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_user_info;
    }

    @Override
    protected void initView() {
        setTitle("详细资料");
        tvAccount = rootView.findViewById(R.id.tv_account);
        btnSend = rootView.findViewById(R.id.btn_send_msg);
        tvNick = rootView.findViewById(R.id.tv_nick);
        tvNote = rootView.findViewById(R.id.tv_name);
        btnNote = rootView.findViewById(R.id.btn_update_note);
        btnAddFriend = rootView.findViewById(R.id.btn_add_friend);
        ivAvatar = rootView.findViewById(R.id.iv_avatar);
        btnSend.setOnClickListener(this);
        btnNote.setOnClickListener(this);
        btnAddFriend.setOnClickListener(this);
    }

    @Override
    protected void initData() {

        AsyncExecutor.getInstance().execute(new OnExecuteCallback<User>() {
            @Override
            public User onExecute() throws Exception {
                return XMPPHelper.getInstance().getUserInfo(account);
            }

            @Override
            public void onFinish(User userInfo, Exception e) {
                if (e != null) {
                    Logger.e(e);
                    return;
                }
                user = userInfo;
                if (user != null) {
                    String userAccount = userInfo.getAccount().split("@")[0];
                    String nick = userInfo.getNickName();
                    String userNote = userInfo.getNote();
                    String avatar = userInfo.getAvatar();
                    int sex = userInfo.getSex();

                    tvAccount.setText("账号：" + userAccount);
                    if (!TextUtils.isEmpty(userNote)) {
                        tvNote.setText(userNote);
                    } else if (!TextUtils.isEmpty(note)) {
                        tvNote.setText(note);
                    } else {
                        tvNote.setText("未填写");
                    }
                    if (sex == 0) {
                        tvNote.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_man, 0);
                    } else if (sex == 1) {
                        tvNote.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_woman, 0);
                    } else {
                        tvNote.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    }
                    tvNick.setText("昵称：" + (!TextUtils.isEmpty(nick) ? nick : userAccount));

                    if (avatar != null) {
                        ImageUtil.showImage(getActivity(), ivAvatar, avatar);
                    }
                } else {
                    tvAccount.setText("账号：" + account.split("@")[0]);
                    tvNote.setText(note);
                    tvNick.setText("昵称：");
                    tvNote.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                }
                checkIsFriend();
            }
        });
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_msg:
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                intent.putExtra("targetUser", account);
                intent.putExtra("msgDb", account.split("@")[0]);
                intent.putExtra("type", AppConstants.ChatType.SINGLE);
                if (user != null && !TextUtils.isEmpty(user.getNickName())) {
                    intent.putExtra("title", user.getNickName());
                } else {
                    intent.putExtra("title", account.split("@")[0]);
                }
                startActivity(intent);
                break;
            case R.id.btn_update_note:
                break;
            case R.id.btn_add_friend:
                addFriend(account);
                break;
        }
    }

    /**
     * 添加好友
     *
     * @param account
     */
    private void addFriend(final String account) {
        try {
            XMPPHelper.getInstance().applyFriend(account);
            Toast.makeText(getActivity(), "发送申请成功", Toast.LENGTH_SHORT).show();
            pop();
        } catch (SmackException.NotConnectedException ex) {
            Logger.e(ex);
            Toast.makeText(getActivity(), "连接服务器失败", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Logger.e(e);
            Toast.makeText(getActivity(), "发送申请失败", Toast.LENGTH_SHORT).show();
        }
//        AsyncExecutor.getInstance().execute(new OnExecuteCallback<Boolean>() {
//            @Override
//            public Boolean onExecute() throws Exception {
//                boolean flag = XMPPHelper.getInstance().applyFriend(account);
//                return flag;
//            }
//
//            @Override
//            public void onFinish(Boolean result, Exception e) {
//                if (e != null) {
//                    Logger.e(e);
//                    Toast.makeText(getActivity(), "连接服务器失败", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (result) {
//                    Toast.makeText(getActivity(), "发送申请成功", Toast.LENGTH_SHORT).show();
//                    pop();
//                } else {
//                    Toast.makeText(getActivity(), "发送申请失败", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    /**
     * 检查是否是好友
     */
    public void checkIsFriend() {
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

                for (User u : result) {
                    if (u.getAccount().equals(account)) {
                        //是好友
                        switchView(true);
                        return;
                    }
                }
                switchView(false);

            }

        });
    }

    private void switchView(boolean isFriend) {
        btnNote.setVisibility(isFriend ? View.VISIBLE : View.GONE);
        btnSend.setVisibility(isFriend ? View.VISIBLE : View.GONE);
        btnAddFriend.setVisibility(isFriend ? View.GONE : View.VISIBLE);
    }

}
