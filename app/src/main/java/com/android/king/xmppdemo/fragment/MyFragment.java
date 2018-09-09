package com.android.king.xmppdemo.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SPUtil;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import me.yokeyword.fragmentation.SupportFragment;


/**
 * 我
 */
public class MyFragment extends SupportFragment implements View.OnClickListener {

    private TextView tvSetting;
    private TextView tvName;
    private TextView tvAccount;
    private ImageView ivAvatar;
    private View btnMyInfo;

    private User user;

    public static MyFragment newInstance() {
        MyFragment fragment = new MyFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my, container, false);
        ivAvatar = v.findViewById(R.id.iv_avatar);
        tvName = v.findViewById(R.id.tv_name);
        tvAccount = v.findViewById(R.id.tv_account);
        btnMyInfo = v.findViewById(R.id.ll_my_info);
        tvSetting = v.findViewById(R.id.tv_setting);
        tvSetting.setOnClickListener(this);
        btnMyInfo.setOnClickListener(this);
        loadData();
        return v;
    }


    public void loadData() {
        final String account = SPUtil.getString(getActivity(), AppConstants.SP_KEY_LOGIN_ACCOUNT);
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
                    user = userInfo;
                    String userAccount = userInfo.getAccount();
                    String name = userInfo.getName();
                    Bitmap avatar = userInfo.getAvatar();
                    if (!TextUtils.isEmpty(userAccount)) {
                        tvAccount.setText("账号：" + userAccount);
                    } else {
                        tvAccount.setText("账号：" + account);
                    }
                    if (!TextUtils.isEmpty(name)) {
                        tvName.setText(name);
                    } else {
                        tvName.setText(account);
                    }
                    if (avatar != null) {
                        ivAvatar.setImageBitmap(avatar);
                    }
                } else {
                    user = new User();
                    user.setAccount(account);
                    user.setName(account.split("@")[0]);
                    user.setNickName(account.split("@")[0]);
                    tvAccount.setText("账号：" + account);
                    tvName.setText(account.split("@")[0]);
                    ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_setting:
                ((HomeFragment) getParentFragment()).start(SettingFragment.newInstance());
                break;
            case R.id.ll_my_info:
                if (user == null) {
                    return;
                }
                ((HomeFragment) getParentFragment()).start(MyInfoFragment.newInstance(user));
                break;
        }
    }
}
