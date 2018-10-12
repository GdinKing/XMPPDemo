package com.android.king.xmppdemo.ui;


import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

/***
 * 注册界面
 *
 * @since 2018-09-04
 * @author king
 */
public class RegistActivity extends BaseActivity {

    private EditText etAccount;
    private EditText etPassword;

    private Button btnRegist;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_regist;
    }

    @Override
    protected void initView() {
        setTitle("注册");
        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        btnRegist = findViewById(R.id.btn_regist);
        btnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (TextUtils.isEmpty(account)) {
                    Toast.makeText(RegistActivity.this, "账号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegistActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                doRegist(account, password);
            }
        });
    }

    @Override
    protected void initData() {

    }

    /**
     * 注册
     *
     * @param account
     * @param password
     */
    private void doRegist(final String account, final String password) {
        showLoading();
        AsyncExecutor.getInstance().execute(new OnExecuteCallback<Void>() {
            @Override
            public Void onExecute() throws Exception {
                XMPPHelper.getInstance().regist(account, password, null);
                return null;
            }

            @Override
            public void onFinish(Void result, Exception e) {
                hideLoading();
                if (e != null) {
                    Logger.e(e);
                    Toast.makeText(RegistActivity.this, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(RegistActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }


}
