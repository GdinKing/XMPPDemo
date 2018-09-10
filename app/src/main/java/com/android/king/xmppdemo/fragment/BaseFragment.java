package com.android.king.xmppdemo.fragment;

import android.app.ProgressDialog;
import android.king.xmppdemo.R;
import com.android.king.xmppdemo.listener.OnBackClickListener;
import com.android.king.xmppdemo.view.TipDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    protected TipDialog tipDialog;

    protected ImageView ivBack;
    protected TextView tvTitle;
    protected OnBackClickListener backClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getContentView(), container, false);
        initTitle();
        initView();
        initData();
        return rootView;
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

    protected void showTip(String msg) {
        if (tipDialog == null) {
            tipDialog = new TipDialog(getActivity());
            tipDialog.setTipMessage(msg);
            tipDialog.enableNegative(false);
        }
        tipDialog.setOnTipClickListener(new TipDialog.OnTipClickListener() {
            @Override
            public void onPositiveClick() {
                tipDialog.dismiss();
            }

            @Override
            public void onNegativeClick() {
                tipDialog.dismiss();
            }
        });
        if (!tipDialog.isShowing()) {
            tipDialog.show();
        }
    }

    protected void showTip(String msg, TipDialog.OnTipClickListener listener) {
        if (tipDialog == null) {
            tipDialog = new TipDialog(getActivity());
            tipDialog.setTipMessage(msg);
        }
        tipDialog.setOnTipClickListener(listener);
        if (!tipDialog.isShowing()) {
            tipDialog.show();
        }
    }

    protected void hideTip() {
        if (tipDialog != null && !tipDialog.isShowing()) {
            tipDialog.dismiss();
        }
    }

    protected abstract int getContentView();

    protected abstract void initView();

    protected abstract void initData();
}