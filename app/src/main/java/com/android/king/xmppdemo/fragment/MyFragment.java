package com.android.king.xmppdemo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.king.xmppdemo.BaseApplication;
import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.event.UpdateInfoEvent;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.util.ImageUtil;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
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

    private boolean isLoading = false;


    public void loadData() {
        final String account = BaseApplication.getCurrentLogin();
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback<User>() {
            @Override
            public User onExecute() throws Exception {
                isLoading = true;
                return XMPPHelper.getInstance().getUserInfo(account);
            }

            @Override
            public void onFinish(User userInfo, Exception e) {
                isLoading = false;
                if (e != null) {
                    Logger.e(e);
                    return;
                }
                user = userInfo;
                setView(userInfo);
                if (user == null) {
                    user = new User();
                    user.setAccount(account);
                    user.setName(account.split("@")[0]);
                    user.setNickName(account.split("@")[0]);
                }
            }
        });
    }

    private void setView(User userInfo) {
        String account = BaseApplication.getCurrentLogin();
        if (userInfo != null) {
            String userAccount = userInfo.getAccount();
            String name = userInfo.getNickName();
            String avatar = userInfo.getAvatar();
            int sex = userInfo.getSex();
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
            if (sex == 0) {
                tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_man, 0);
            } else if (sex == 1) {
                tvName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_woman, 0);
            }
            if (avatar != null) {
                ImageUtil.showImage(getActivity(), ivAvatar, user.getAvatar());
            } else {
                Logger.i("头像为空");
            }
        } else {

            tvAccount.setText("账号：" + account);
            tvName.setText(account.split("@")[0]);
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_setting:
                ((HomeFragment) getParentFragment()).start(SettingFragment.newInstance());
                break;
            case R.id.ll_my_info:
                if (user == null || isLoading) {
                    return;
                }
                ((HomeFragment) getParentFragment()).start(MyInfoFragment.newInstance(user));
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateInfoEvent(UpdateInfoEvent event) {
        user = event.user;
        setView(user);
    }
}
