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
    public static final String SP_KEY_LOGIN_PASSWORD = "login_password";


    public static final String INTENT_KEY_ADD_FRIEND = "add_friend";
    public static final String INTENT_KEY_ADD_FRIEND_FROM = "add_friend_from";
    public static final String ACTION_FRIEND = "android.king.xmpp.friend";
    public static final int STATUS_ADD_FRIEND_OK = 0;//通过申请
    public static final int STATUS_ADD_FRIEND_RECEIVE = 1;//收到申请
    public static final int STATUS_ADD_FRIEND_REJECJ = 2; //拒绝
    public static final int STATUS_ADD_FRIEND_ERROR = -1;


    public static final String ACTION_INCOME_MESSAGE = "android.king.xmpp.message.income";
    public static final String ACTION_RECONNECT_ERROR = "android.king.xmpp.reconnect.error";

    public static final String TABLE_APPLY = "apply";
    //创建apply表
    public static final String CREATE_TABLE_APPLY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_APPLY + "(" +
                    "id INTEGER PRIMARY KEY autoincrement," +
                    "fromUser TEXT NOT NULL," +
                    "isAgree INTEGER," +
                    "name TEXT" +
                    ")";


    public static final String TABLE_MESSAGE = "message";
    /**
     * 消息表
     * contentFile指得是对应的消息内容缓存文件名,聊天记录以xml文件形式缓存在本地
     * type:0-文字  1-图片 2-语音  3-视频
     */
    public static final String CREATE_TABLE_MESSAGE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGE + "(" +
                    "id INTEGER PRIMARY KEY autoincrement," +
                    "fromUser TEXT NOT NULL," +
                    "contentFile TEXT," +
                    "type INTEGER NOT NULL," +
                    "time LONG" +
                    ")";

    public static final String TABLE_CHAT = "chat";
    /**
     * 聊天列表
     * <p>
     * type:0-单聊  1-群聊
     */
    public static final String CREATE_TABLE_CHAT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CHAT + "(" +
                    "id INTEGER PRIMARY KEY autoincrement," +
                    "fromUser TEXT NOT NULL," +
                    "avatar TEXT," +
                    "message TEXT," +
                    "type INT NOT NULL," +
                    "unread INTEGER," +
                    "time LONG" +
                    ")";

    public static class FriendStatus {
        public static final String SUBSCRIBE = "subscribe";
        public static final String SUBSCRIBED = "subscribed";
        public static final String UNSUBSCRIBE = "unsubscribe";
    }
}
