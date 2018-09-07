package com.android.king.xmppdemo.fragment;

import android.graphics.Bitmap;
import android.king.xmppdemo.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
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
    private TextView tvName;
    private ImageView ivAvatar;

    private User userInfo;
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
        tvName = rootView.findViewById(R.id.tv_name);
        ivAvatar = rootView.findViewById(R.id.iv_avatar);
        btnSend.setOnClickListener(this);
    }

    @Override
    protected void initData() {

        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback() {
            @Override
            public void onExecute() throws Exception {
                userInfo = XMPPHelper.getInstance().getUserInfo(account + "@" + XMPPHelper.SERVER_DOMAIN);
            }

            @Override
            public void onFinish(Exception e) {
                if (e != null) {
                    Logger.e(e);
                    return;
                }
                if (userInfo != null) {
                    Logger.i("获取用户信息" + userInfo.toString());
                    String userAccount = userInfo.getAccount().split("@")[0];
                    String nick = userInfo.getNickName();
                    String userNote = userInfo.getNote();
                    Bitmap avatar = userInfo.getAvatar();
                    int sex = userInfo.getSex();

                    tvAccount.setText("账号：" + userAccount);
                    tvName.setText(!TextUtils.isEmpty(userNote) ? userNote : note);
                    if (sex == 0) {
                        tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_man, 0);
                    } else if (sex == 1) {
                        tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_woman, 0);
                    } else {
                        tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    }
                    tvNick.setText(!TextUtils.isEmpty(nick) ? nick : userAccount);

                    if (avatar != null) {
                        ivAvatar.setImageBitmap(avatar);
                    }
                } else {
                    tvAccount.setText("账号：" + account);
                    tvName.setText(account);
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

    }
}
