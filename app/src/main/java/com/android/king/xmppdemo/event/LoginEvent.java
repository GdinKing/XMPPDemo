package com.android.king.xmppdemo.event;

/***
 * 登录事件
 * @since 2018-09-14
 * @author king
 */
public class LoginEvent {
    public boolean isLogin;

    public LoginEvent(boolean isLogin) {
        this.isLogin = isLogin;
    }
}
