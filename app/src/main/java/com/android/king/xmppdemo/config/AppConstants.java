package com.android.king.xmppdemo.config;

/***
 * 常量
 *
 * @since 2018-09-05
 * @author king
 */
public class AppConstants {

    public static final int RECONNECT_DELAY = 10000;//重连延时

    public static final String SP_KEY_LOGIN_STATUS = "login_status";
    public static final String SP_KEY_LOGIN_ACCOUNT = "login_account";
    public static final String SP_KEY_LOGIN_PASSWORD = "login_password";
    public static final String SP_KEY_SOFT_INPUT_HEIGHT = "soft_input_height";

    public static class StanzaStatus {
        public static final String SUBSCRIBE = "subscribe";  //收到申请
        public static final String SUBSCRIBED = "subscribed";  //通过申请
        public static final String UNSUBSCRIBE = "unsubscribe";//拒绝
        public static final String UNAVAILABLE = "unavailable";//离线
        public static final String AVAILABLE = "available";//在线
    }

    public static class ChatType {
        public static final int SINGLE = 0;  //单聊
        public static final int MULTI = 1;  //群聊
        public static final int MULTI_INVITE = 2;  //群聊邀请
        public static final int SERVER_MSG = 3;  //系统消息
    }

    public static class MessageStatus {
        public static final int SUCCESS = 0;  //成功
        public static final int ERROR = 1;  //失败
    }

    public static class MessageType {
        public static final int IN_TEXT = 0;  //对方文字
        public static final int OUT_TEXT = 1;  //我的文字

        public static final int IN_IMAGE = 2;  //对方图片
        public static final int OUT_IMAGE = 3;  //我的图片
    }

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
     * type:0-文字  1-图片 2-语音  3-视频
     * category: 参考上面MessageType
     * status：0发送成功  1未发送
     */
    public static final String CREATE_TABLE_MESSAGE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGE + "(" +
                    "id INTEGER PRIMARY KEY autoincrement," +
                    "fromUser TEXT NOT NULL," +
                    "toUser TEXT NOT NULL," +
                    "content TEXT," +
                    "type INTEGER NOT NULL," +
                    "category INTEGER NOT NULL," +
                    "status INTEGER NOT NULL," +
                    "time LONG" +
                    ")";

    public static final String TABLE_CHAT = "chat";
    /**
     * 聊天列表
     * <p>
     * msgDb指的是对应的聊天记录缓存数据库名,不同好友的聊天记录缓存在不同的SQLite中
     * type:0-单聊  1-群聊
     */
    public static final String CREATE_TABLE_CHAT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CHAT + "(" +
                    "id INTEGER PRIMARY KEY autoincrement," +
                    "fromUser TEXT NOT NULL," +
                    "avatar TEXT," +
                    "title TEXT," +
                    "message TEXT," +
                    "msgDb TEXT," +
                    "type INTEGER NOT NULL," +
                    "unread INTEGER," +
                    "level INTEGER," +
                    "time LONG" +
                    ")";


    public static final String TABLE_FRINED = "friend";
    /**
     * 好友表
     */
    public static final String CREATE_TABLE_FRIEND =
            "CREATE TABLE IF NOT EXISTS " + TABLE_FRINED + "(" +
                    "id INTEGER PRIMARY KEY autoincrement," +
                    "name TEXT," +
                    "avatar TEXT," +
                    "nickname TEXT," +
                    "account TEXT NOT NULL," +
                    "note TEXT," +
                    "sign TEXT" +
                    ")";

    public static final String FRIEND_CHANNEL_ID = "FRIEND_CHANNEL_ID";
    public static final String FRIEND_CHANNEL_NAME = "FRIEND_CHANNEL_NAME";
    public static final String MESSAGE_CHANNEL_ID = "MESSAGE_CHANNEL_ID";
    public static final String MESSAGE_CHANNEL_NAME = "MESSAGE_CHANNEL_NAME";
    public static final String ACTION_RECEIVE_MESSAGE = "action.receive.message";
    public static final int FRIEND_NOTIFY_ID = 0;
    public static final int MESSAGE_NOTIFY_ID = 1;


    public static final String INFO_URL = "https://info.3g.qq.com";
}
