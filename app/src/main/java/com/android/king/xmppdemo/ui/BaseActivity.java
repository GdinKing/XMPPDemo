package com.android.king.xmppdemo.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
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

import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.SupportHelper;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月06日 16:22分
 * @since 2018-09-06
 * @author king
 */
public abstract class BaseActivity extends SupportActivity {

    protected ProgressDialog progressDialog;

    protected ImageView ivBack;
    protected TextView tvTitle;
    protected OnBackClickListener backClickListener;

    protected Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        this.mContext = this;
        initTitle();
        initView();
        initData();
    }

    protected String getCurrentLogin() {
        return SPUtil.getString(this, AppConstants.SP_KEY_LOGIN_ACCOUNT);
    }

    protected void initTitle() {
        try {
            ivBack = findViewById(R.id.iv_back);
            tvTitle = findViewById(R.id.tv_title);

            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (backClickListener != null) {
                        backClickListener.onBackClick();
                    } else {
                        finish();
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


    protected void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    protected void showLoading(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(msg);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    protected void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    protected void showTip(String msg, final OnTipDialogListener listener) {
        new CircleDialog.Builder(this)
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
        new CircleDialog.Builder(this)
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

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void hideSoftInput() {
        Activity activity = this;
        if (activity == null) return;
        View view = activity.getWindow().getDecorView();
        SupportHelper.hideSoftInput(view);
    }
    public void showSoftInput(EditText view) {
        SupportHelper.showSoftInput(view);
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();
}
