package com.android.king.xmppdemo.fragment;

import android.graphics.Bitmap;
import android.king.xmppdemo.R;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.listener.OnNetworkExecuteListener;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SPUtil;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.Set;

import me.yokeyword.fragmentation.SupportFragment;


/**
 * 我
 */
public class MyFragment extends SupportFragment implements View.OnClickListener {

    private TextView tvSetting;
    private TextView tvName;
    private TextView tvAccount;
    private ImageView ivAvatar;

    public static MyFragment newInstance() {
        MyFragment fragment = new MyFragment();
        return fragment;
    }

    private VCard accountInfo;
    private Bitmap avatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my, container, false);
        ivAvatar = v.findViewById(R.id.iv_avatar);
        tvName = v.findViewById(R.id.tv_name);
        tvAccount = v.findViewById(R.id.tv_account);
        tvSetting = v.findViewById(R.id.tv_setting);
        tvSetting.setOnClickListener(this);

        loadData();
        return v;
    }


    public void loadData() {
        final String account = SPUtil.getString(getActivity(), AppConstants.SP_KEY_LOGIN_ACCOUNT);
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteListener() {
            @Override
            public void onExecute() throws Exception {
                Logger.i("账号：" + account);
                accountInfo = XMPPHelper.getInstance().getUserVCard(account);
                avatar = XMPPHelper.getInstance().getUserAvatar(account);
            }

            @Override
            public void onFinish(Exception e) {
                if (e != null) {
                    Logger.e(e);
                    return;
                }
                if (accountInfo != null) {
                    Logger.i("获取用户信息" + accountInfo.toString());
                    String jid = accountInfo.getJabberId();
                    String nick = accountInfo.getNickName();
                    if (!TextUtils.isEmpty(jid)) {
                        tvAccount.setText("账号：" + jid.split("@")[0]);
                    } else {
                        tvAccount.setText("账号：" + account);
                    }
                    if (!TextUtils.isEmpty(nick)) {
                        tvName.setText(nick);
                    } else {
                        tvName.setText(account);
                    }
                }
                if (avatar != null) {
                    ivAvatar.setImageBitmap(avatar);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_setting:
                start(SettingFragment.newInstance());
                break;
        }
    }
}
