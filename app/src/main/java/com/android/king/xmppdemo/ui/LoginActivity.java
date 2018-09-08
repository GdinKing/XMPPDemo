package com.android.king.xmppdemo.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SPUtil;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;


/***
 * 登录界面
 *
 * @since 2018-09-04
 * @author king
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etAccount;
    private EditText etPassword;

    private Button btnLogin;
    private TextView tvRegist;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();

    }

    private void initView() {
        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegist = findViewById(R.id.tv_regist);
        tvRegist.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                String account = etAccount.getText().toString();
                String password = etPassword.getText().toString();
                if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "账号密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                doLogin(account, password);
                break;
            case R.id.tv_regist:
                startActivity(new Intent(this, RegistActivity.class));
                break;

        }
    }

    /**
     * 登录
     *
     * @param account
     * @param password
     */
    private void doLogin(final String account, final String password) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("登录中...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();

        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {

                AbstractXMPPConnection conn = XMPPHelper.getInstance().getConnection();
                if (conn == null) {
                    conn = XMPPHelper.getInstance().openConnection();
                }
                conn.addConnectionListener(new ConnectionListener() {
                    @Override
                    public void connected(XMPPConnection xmppConnection) {
                        Logger.i("连接了");
                    }

                    @Override
                    public void authenticated(XMPPConnection xmppConnection, boolean b) {
                        mHandler.sendEmptyMessage(200);
                        try {
                            Presence presence = new Presence(Presence.Type.available);//在线
                            xmppConnection.sendStanza(presence);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void connectionClosed() {

                    }

                    @Override
                    public void connectionClosedOnError(Exception e) {

                    }

                    @Override
                    public void reconnectionSuccessful() {

                    }

                    @Override
                    public void reconnectingIn(int i) {

                    }

                    @Override
                    public void reconnectionFailed(Exception e) {

                    }
                });
                XMPPHelper.getInstance().login(account, password, "android");


                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {

                if (e != null) {
                    Logger.e(e);
                    if (e.getMessage().contains("not-authorized")) {//账号密码错误
                        Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "登录失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                SPUtil.setBoolean(LoginActivity.this, AppConstants.SP_KEY_LOGIN_STATUS, true);
                SPUtil.setString(LoginActivity.this, AppConstants.SP_KEY_LOGIN_ACCOUNT, etAccount.getText().toString());
                SPUtil.setString(LoginActivity.this, AppConstants.SP_KEY_LOGIN_PASSWORD, etPassword.getText().toString());
                Intent it = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(it);
                finish();
            }
        });

    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (msg.what == 200) {
                SPUtil.setBoolean(LoginActivity.this, AppConstants.SP_KEY_LOGIN_STATUS, true);
                SPUtil.setString(LoginActivity.this, AppConstants.SP_KEY_LOGIN_ACCOUNT, etAccount.getText().toString());
                SPUtil.setString(LoginActivity.this, AppConstants.SP_KEY_LOGIN_PASSWORD, etPassword.getText().toString());

                Intent it = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(it);
                finish();
            } else if (msg.what == 404) {
                Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 500) {
                Toast.makeText(LoginActivity.this, "登录失败，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        }


    };

}
