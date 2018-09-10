package com.android.king.xmppdemo.fragment;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.listener.OnBackClickListener;
import com.android.king.xmppdemo.listener.OnTipDialogListener;
import com.android.king.xmppdemo.util.SPUtil;
import com.mylhyl.circledialog.CircleDialog;
import com.mylhyl.circledialog.callback.ConfigTitle;
import com.mylhyl.circledialog.params.TitleParams;
import com.mylhyl.circledialog.res.values.CircleDimen;

import me.yokeyword.fragmentation.SupportFragment;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 17:28分
 * @since 2018-09-04
 * @author king
 */
public abstract class BaseFragment extends SupportFragment {

    protected View rootView;
    protected ProgressDialog progressDialog;
    protected ImageView ivBack;
    protected TextView tvTitle;
    protected OnBackClickListener backClickListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getContentView(), container, false);
        initTitle();
        initView();
        initData();
        return rootView;
    }

    protected String getCurrentLogin(){
        return SPUtil.getString(getActivity(), AppConstants.SP_KEY_LOGIN_ACCOUNT);
    }

    protected void initTitle() {
        try {
            ivBack = rootView.findViewById(R.id.iv_back);
            tvTitle = rootView.findViewById(R.id.tv_title);

            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (backClickListener != null) {
                        backClickListener.onBackClick();
                    } else {
                        pop();
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    protected void setTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }


    protected void setBackClickListener(OnBackClickListener listener) {
        this.backClickListener = listener;
    }

    protected void showProgress(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(msg);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    protected void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    protected void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }


    protected void showTip(String msg, final OnTipDialogListener listener) {
        new CircleDialog.Builder(getActivity())
                .setTitle("提示")
                .setTitleColor(Color.BLACK)
                .configTitle(new ConfigTitle() {
                    @Override
                    public void onConfig(TitleParams params) {
                        params.textSize = CircleDimen.SUBTITLE_TEXT_SIZE;
                    }
                })
                .setText(msg)
                .setCanceledOnTouchOutside(false)
                .setCancelable(false)
                .setPositive("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onPositiveClick();
                        }
                    }
                })
                .setNegative("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onNegativeClick();
                        }
                    }
                }).show();
    }

    protected void showSimpleTip(String msg) {
        new CircleDialog.Builder(getActivity())
                .setTitle("提示")
                .setTitleColor(Color.BLACK)
                .configTitle(new ConfigTitle() {
                    @Override
                    public void onConfig(TitleParams params) {
                        params.textSize = CircleDimen.SUBTITLE_TEXT_SIZE;
                    }
                })
                .setText(msg)
                .setCanceledOnTouchOutside(false)
                .setNegative("确定", null).show();
    }

    protected abstract int getContentView();

    protected abstract void initView();

    protected abstract void initData();

}
