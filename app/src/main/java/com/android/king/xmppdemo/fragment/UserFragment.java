package com.android.king.xmppdemo.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.ui.MessageActivity;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;


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

    private TextView btnSend;
    private TextView tvAccount;
    private TextView tvNick;
    private TextView tvNote; //备注名
    private ImageView ivAvatar;

    private String account;
    private String note;

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
        ivAvatar = rootView.findViewById(R.id.iv_avatar);
        btnSend.setOnClickListener(this);
    }

    @Override
    protected void initData() {

        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback<User>() {
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
                if (userInfo != null) {
                    String userAccount = userInfo.getAccount().split("@")[0];
                    String nick = userInfo.getNickName();
                    String userNote = userInfo.getNote();
                    Bitmap avatar = userInfo.getAvatar();
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
                        ivAvatar.setImageBitmap(avatar);
                    }
                } else {
                    tvAccount.setText("账号：" + account.split("@")[0]);
                    tvAccount.setText("tvNick：" + account.split("@")[0]);
                    tvNote.setText(account);
                    ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                }

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
                intent.putExtra("targetUser",account);
                intent.putExtra("msgDb", account.split("@")[0]);
                intent.putExtra("type",AppConstants.ChatType.SINGLE);
                startActivity(intent);
                break;
        }
    }
}
