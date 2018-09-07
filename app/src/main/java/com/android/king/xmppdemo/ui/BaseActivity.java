package com.android.king.xmppdemo.ui;

import android.app.ProgressDialog;
import android.king.xmppdemo.R;

import com.android.king.xmppdemo.listener.OnBackClickListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.yokeyword.fragmentation.SupportActivity;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月06日 16:22分
 * @since 2018-09-06
 * @author king
 */
public abstract class BaseActivity extends SupportActivity {

    protected View rootView;
    protected ProgressDialog progressDialog;

    protected ImageView ivBack;
    protected TextView tvTitle;
    protected OnBackClickListener backClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initTitle();
        initView();
        initData();
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


    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();
}
