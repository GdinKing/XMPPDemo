package com.android.king.xmppdemo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SPUtil;
import com.android.king.xmppdemo.util.SoftInputUtil;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import java.util.ArrayList;
import java.util.List;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;


/***
 * 登录界面
 *
 * @since 2018-09-04
 * @author king
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText etAccount;
    private EditText etPassword;

    private View btnLogin;
    private View tvRegist;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegist = findViewById(R.id.tv_regist);
        tvRegist.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        SoftInputUtil.getSoftKeyboardHeight(getWindow().getDecorView(), new SoftInputUtil.OnGetSoftHeightListener() {
            @Override
            public void onShowed(int height) {
                //保存软键盘高度
                SPUtil.setInt(LoginActivity.this, AppConstants.SP_KEY_SOFT_INPUT_HEIGHT, height);
            }
        });
        //权限申请
        List<PermissionItem> permissionItems = new ArrayList<>();
        permissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储", R.drawable.permission_ic_storage));
        permissionItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE, "设备", R.drawable.permission_ic_phone));
        HiPermission.create(this)
                .permissions(permissionItems)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                    }

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onDeny(String permission, int position) {

                    }

                    @Override
                    public void onGuarantee(String permission, int position) {

                    }
                });
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
        showLoading("登录中...");
        AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
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
                XMPPHelper.getInstance().login(account, password);
                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {
                hideLoading();
                if (e != null) {
                    Logger.e(e);
                    if (e.getMessage().contains("not-authorized")) {//账号密码错误
                        Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "登录失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

            }
        });

    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 200) {
                XMPPHelper.getInstance().changeStatus(AppConstants.StanzaStatus.AVAILABLE);
                SPUtil.setBoolean(LoginActivity.this, AppConstants.SP_KEY_LOGIN_STATUS, true);
                SPUtil.setString(LoginActivity.this, AppConstants.SP_KEY_LOGIN_ACCOUNT, etAccount.getText().toString());
                SPUtil.setString(LoginActivity.this, AppConstants.SP_KEY_LOGIN_PASSWORD, etPassword.getText().toString());
                Intent it = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(it);
                finish();
            }
        }


    };

}
