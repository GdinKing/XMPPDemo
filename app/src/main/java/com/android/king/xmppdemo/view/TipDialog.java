package com.android.king.xmppdemo.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.king.xmppdemo.R;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/***
 * 提示对话框
 * @since 2018-09-07
 * @author king
 */
public class TipDialog extends Dialog {


    private BorderTextView btnCancel;
    private BorderTextView btnOk;
    private TextView tvTitle;
    private TextView tvMsg;
    private OnTipClickListener tipClickListener;

    public TipDialog(@NonNull Context context) {
        super(context, R.style.TipDialog);
        init(context);
    }


    private void init(Context context) {

        View v = LayoutInflater.from(context).inflate(R.layout.dialog_tip, null);
        btnOk = v.findViewById(R.id.tv_ok);
        btnCancel = v.findViewById(R.id.tv_cancel);
        tvTitle = v.findViewById(R.id.tv_title);
        tvMsg = v.findViewById(R.id.tv_message);

        setContentView(v);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        btnCancel.setBorderColor(ContextCompat.getColor(context, R.color.blue));
        btnCancel.setTextColor(ContextCompat.getColor(context, R.color.blue));
        btnCancel.setSolidColor(Color.WHITE);
        btnOk.setFullColor(ContextCompat.getColor(context, R.color.blue));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipClickListener != null) {
                    tipClickListener.onPositiveClick();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tipClickListener != null) {
                    tipClickListener.onNegativeClick();
                }
            }
        });
    }

    public void setNegativeText(String msg) {
        btnCancel.setText(msg);
    }

    public void setPositiveText(String msg) {
        btnOk.setText(msg);
    }

    public void setTipMessage(String msg) {
        tvMsg.setText(msg);
    }

    public void setTipTitle(String title) {
        tvTitle.setText(title);
    }

    public void enableNegative(boolean flag) {
        btnCancel.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void show() {
        super.show();
        getWindow().setWindowAnimations(R.style.TipDialogAnim); //设置窗口弹出动画
    }

    public void setOnTipClickListener(OnTipClickListener listener) {
        this.tipClickListener = listener;
    }

    public interface OnTipClickListener {
        void onPositiveClick();

        void onNegativeClick();
    }
}
