package com.android.king.xmppdemo.fragment;

import android.content.Intent;
import android.king.xmppdemo.R;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.listener.OnNetworkExecuteListener;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.ui.LoginActivity;
import com.android.king.xmppdemo.util.SPUtil;
import com.android.king.xmppdemo.view.TipDialog;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import android.view.View;
import android.widget.TextView;


/**
 * 设置
 */
public class SettingFragment extends BaseFragment implements View.OnClickListener{

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    private TextView tvLogout;

    @Override
    protected int getContentView() {
        return R.layout.fragment_setting;
    }

    @Override
    protected void initView() {
        setTitle("添加好友");
        tvLogout = rootView.findViewById(R.id.tv_logout);
        tvLogout.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }


    /**
     * 退出登录
     */
    private void logout() {
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteListener() {
            @Override
            public void onExecute() throws Exception {
                XMPPHelper.getInstance().logout();

            }

            @Override
            public void onFinish(Exception e) {
                if(e!=null){
                    return;
                }
                SPUtil.setBoolean(getActivity(), AppConstants.SP_KEY_LOGIN_STATUS,false);
                SPUtil.setString(getActivity(), AppConstants.SP_KEY_LOGIN_ACCOUNT, "");
                SPUtil.setString(getActivity(), AppConstants.SP_KEY_LOGIN_PASSWOrD, "");
                showToast("退出成功");
                startActivity(new Intent(getActivity(), LoginActivity.class));
                pop();
            }
        });
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.tv_logout:
                showTip("是否退出登录？", new TipDialog.OnTipClickListener() {
                    @Override
                    public void onPositiveClick() {
                        logout();
                    }

                    @Override
                    public void onNegativeClick() {

                    }
                });
                break;
        }
    }
}
