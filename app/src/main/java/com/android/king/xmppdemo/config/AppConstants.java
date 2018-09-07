package com.android.king.xmppdemo.config;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月05日 16:35分
 * @since 2018-09-05
 * @author king
 */
public class AppConstants {

    public static final String SP_KEY_LOGIN_STATUS = "login_status";
    public static final String SP_KEY_LOGIN_ACCOUNT = "login_account";
    public static final String SP_KEY_LOGIN_PASSWOrD = "login_password";


    public static final String INTENT_KEY_ADD_FRIEND = "add_friend";
    public static final String INTENT_KEY_ADD_FRIEND_FROM = "add_friend_from";
    public static final String ACTION_FRIEND = "android.king.xmpp.friend";
    public static final int STATUS_ADD_FRIEND_OK = 0;//通过申请
    public static final int STATUS_ADD_FRIEND_RECEIVE = 1;//收到申请
    public static final int STATUS_ADD_FRIEND_REJECJ = 2; //拒绝


    public static final String ACTION_INCOME_MESSAGE = "android.king.xmpp.message.income";

    public static class FriendStatus{
        public static final String SUBSCRIBE = "subscribe";
        public static final String SUBSCRIBED = "subscribed";
        public static final String UNSUBSCRIBE = "unsubscribe";
    }
}
