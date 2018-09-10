package com.android.king.xmppdemo.xmpp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.util.Logger;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 12:19分
 * @since 2018-09-04
 * @author king
 */
public class XMPPHelper {


    //    public static final String SERVER_DOMAIN = "10.0.1.90";
    public static final String SERVER_DOMAIN = "60.205.185.28";

    private static XMPPHelper mInstance = null;

    private AbstractXMPPConnection xmppConnection = null;

    private XMPPHelper() {
        xmppConnection = openConnection();
    }

    public static XMPPHelper getInstance() {
        if (mInstance == null) {
            synchronized (XMPPHelper.class) {
                if (mInstance == null) {
                    mInstance = new XMPPHelper();
                }
            }
        }
        return mInstance;
    }

    public synchronized AbstractXMPPConnection getConnection() {
        return xmppConnection;
    }

    /**
     * 获取连接信息
     *
     * @return
     */
    public AbstractXMPPConnection openConnection() {
        AbstractXMPPConnection conn = null;
        try {

//            ProviderManager.addIQProvider("query", "jabber:iq:private",
//                    new PrivateDataManager.PrivateDataIQProvider());
//            ProviderManager.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
            InetAddress addr = InetAddress.getByName(SERVER_DOMAIN);
            // 主机名验证
            HostnameVerifier verifier = new HostnameVerifier() {
                public boolean verify(String arg0, SSLSession arg1) {
                    return false;
                }
            };
            DomainBareJid serviceName = JidCreate.domainBareFrom(SERVER_DOMAIN);
            XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration
                    .builder();
            builder.setXmppDomain(serviceName);
            builder.setHostnameVerifier(verifier);
            builder.setHostAddress(addr);
            builder.setPort(5222);
            builder.setSendPresence(true);
            builder.setConnectTimeout(15000);
            builder.setCompressionEnabled(true);
            builder.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled);
            builder.setDebuggerEnabled(false);

            // 自动回复回执方法，如果对方的消息要求回执。
            ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
            ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceiptRequest.Provider());
            DeliveryReceiptManager.getInstanceFor(xmppConnection).autoAddDeliveryReceiptRequests();

            //允许别人添加好友
            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
            ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());
            ProviderManager.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

            XMPPTCPConnectionConfiguration config = builder.build();
            conn = new XMPPTCPConnection(config);
        } catch (Exception e) {
            Logger.e(e);
        }
        return conn;
    }

    /**
     * 登录
     *
     * @throws InterruptedException
     * @throws XMPPException
     * @throws SmackException
     * @throws IOException
     */
    public void login(String username, String password, String resource) throws IOException, InterruptedException, XMPPException, SmackException {
        if (isConnected()) {
            xmppConnection.disconnect();
        }
        xmppConnection.connect();

        SASLAuthentication.blacklistSASLMechanism("ANONYMOUS");
        SASLAuthentication.blacklistSASLMechanism("CRAM-MD5");
        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
        SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1-PLUS");

        xmppConnection.login(username, password, Resourcepart.from(resource));
    }

    /**
     * 修改用户在线状态
     *
     * @param status
     */
    public void changeStatus(String status) {
        try {
            Presence presence = null;
            switch (status) {
                case AppConstants.FriendStatus.AVAILABLE:
                    presence = new Presence(Presence.Type.available);//在线
                    break;
                case AppConstants.FriendStatus.UNAVAILABLE:
                    presence = new Presence(Presence.Type.unavailable);//离线
                    break;
                default:
                    presence = new Presence(Presence.Type.available);//在线
                    break;
            }
//            presence.setTo(userName); 这里可以设置对某人显示离线（即隐身）
            xmppConnection.sendStanza(presence);
        } catch (Exception e) {
            Logger.e(e);
        }
    }

    /**
     * 是否登录
     *
     * @return
     */
    public boolean isLogin() {
        if (xmppConnection != null) {
            return xmppConnection.isConnected() && xmppConnection.isAuthenticated();
        }
        return false;
    }

    /**
     * 是否连接
     *
     * @return
     */
    public boolean isConnected() {
        if (xmppConnection != null) {
            return xmppConnection.isConnected();
        }
        return false;
    }

    /**
     * 退出登录
     */
    public void logout() {
        if (isConnected()) {
            xmppConnection.disconnect();
        }
    }

    /**
     * 创建一个新用户
     *
     * @param account  用户账号
     * @param password 密码
     * @param attr     一些用户资料
     */
    public void registe(String account, String password, Map<String, String> attr) throws InterruptedException, XMPPException, SmackException, IOException {
        if (xmppConnection == null) {
            openConnection();
        }
        if (!xmppConnection.isConnected()) {
            xmppConnection.connect();
        }
        AccountManager manager = AccountManager.getInstance(xmppConnection);
        manager.sensitiveOperationOverInsecureConnection(true);//允许不安全连接
        if (attr == null) {
            manager.createAccount(Localpart.from(account), password);
        } else {
            manager.createAccount(Localpart.from(account), password, attr);
        }
    }

    /**
     * 保存用户信息到VCard
     *
     * @param user
     */
    public void saveUserInfo(User user) throws XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        if (!isLogin()) {
            return;
        }
        VCard vCard = new VCard();
        vCard.setNickName(user.getName());
        vCard.setFirstName(user.getName());
        vCard.setEmailWork(user.getEmail());
        vCard.setJabberId(user.getAccount());
        vCard.setField("sex", String.valueOf(user.getSex()));
        vCard.setField("sign", user.getSign());
        VCardManager.getInstanceFor(xmppConnection).saveVCard(vCard);
    }

    /**
     * 修改密码
     *
     * @return true成功
     */
    public boolean updatePassword(String pwd) {
        if (!isLogin()) {
            return false;
        }
        try {
            AccountManager.getInstance(xmppConnection).changePassword(pwd);
            return true;
        } catch (SmackException | InterruptedException | XMPPException.XMPPErrorException e) {
            Logger.e(e);
            return false;
        }
    }


    /**
     * 直接添加好友到好友列表，不管用户拒绝与否
     *
     * @param account   用户账号
     * @param note      用户备注名
     * @param groupName 所属组名
     * @return
     */
    public boolean addFriend(String account, String note, String groupName) {
        if (xmppConnection != null && xmppConnection.isConnected()) {
            try {
                EntityBareJid userJid = JidCreate.entityBareFrom(account + "@" + SERVER_DOMAIN);
                Roster.getInstanceFor(xmppConnection).createEntry(userJid, note, new String[]{groupName});

                return true;
            } catch (Exception e) {
                Logger.e(e);
                return false;
            }
        }
        throw new NullPointerException("服务器连接失败，请先连接服务器");
    }

    /**
     * 发送添加好友申请
     *
     * @return
     */
    public boolean applyFriend(String account) {
        try {
            if (!account.contains("@")) {
                account = account + "@" + SERVER_DOMAIN;
            }
            Presence presence = new Presence(Presence.Type.subscribe);
            presence.setTo(account);
            xmppConnection.sendStanza(presence);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拒绝好友申请
     *
     * @param account 用户
     */
    public void refuse(String account) {
        try {
            if (!account.contains("@")) {
                account = account + "@" + SERVER_DOMAIN;
            }
            Presence presence = new Presence(Presence.Type.unsubscribed);
            presence.setTo(account);
            xmppConnection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收好友申请
     *
     * @param account 用户
     */
    public void accept(String account) {

        try {
            if (!account.contains("@")) {
                account = account + "@" + SERVER_DOMAIN;
            }
            Presence presence = new Presence(Presence.Type.subscribed);
            presence.setTo(account);
            xmppConnection.sendStanza(presence);

            Presence reSubscription = new Presence(Presence.Type.subscribe);
            reSubscription.setTo(account);
            xmppConnection.sendStanza(reSubscription);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取所有好友
     * 真要搭建即时通讯系统，是不建议这种获取好友的方式的，因为数据不全而且查询麻烦，自己写个Web服务连接数据库进行查询会更适合
     *
     * @return
     */
    public List<User> getAllFriends() throws SmackException.NotLoggedInException, InterruptedException, SmackException.NotConnectedException, XmppStringprepException, XMPPException, SmackException.NoResponseException {
        List<User> userList = new ArrayList<>();
        if (isLogin()) {
            Roster roster = Roster.getInstanceFor(xmppConnection);
            if (!roster.isLoaded()) {
                roster.reloadAndWait();
            }
            Collection<RosterEntry> entries = roster.getEntries();
            for (RosterEntry entry : entries) {
                Logger.i(entry.toString());
                if ("none".equals(entry.getType().name())) {//none表示两者并没有互相订阅
                    continue;
                }
                User user = new User();
                user.setAccount(entry.getJid().toString());
                user.setNote(entry.getName());
                user.setAvatar(getUserAvatar(entry.getJid().toString()));
                userList.add(user);
            }
        }
        return userList;
    }

    /**
     * 获取用户的vcard信息
     *
     * @param account
     * @return
     * @throws XMPPException
     */
    public VCard getUserVCard(String account) throws XMPPException, XmppStringprepException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        if (isConnected() && !TextUtils.isEmpty(account)) {
            if (!account.contains("@")) {
                account = account + "@" + SERVER_DOMAIN;
            }
            VCard vCard = VCardManager.getInstanceFor(xmppConnection).loadVCard(JidCreate.entityBareFrom(account));
            return vCard;
        }
        return null;
    }

    /**
     * 获取用户信息
     *
     * @param account
     * @return
     * @throws XMPPException
     * @throws XmppStringprepException
     * @throws SmackException.NotConnectedException
     * @throws InterruptedException
     * @throws SmackException.NoResponseException
     */
    public User getUserInfo(String account) throws XMPPException, XmppStringprepException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        if (isConnected() && !TextUtils.isEmpty(account)) {
            if (!account.contains("@")) {
                account = account + "@" + XMPPHelper.SERVER_DOMAIN;
            }
            VCard vCard = VCardManager.getInstanceFor(xmppConnection).loadVCard(JidCreate.entityBareFrom(account));

            User user = new User();
            String firstName = vCard.getFirstName();
            if (TextUtils.isEmpty(firstName)) {
                user.setName(vCard.getFirstName());
            } else {
                user.setName(account.split("@")[0]);
            }
            user.setNickName(vCard.getNickName());
            user.setAccount(account);
            user.setEmail(vCard.getEmailWork());
            user.setSign(vCard.getField("sign"));
            user.setAvatar(getUserAvatar(account));
            String sex = vCard.getField("sex");
            if (!TextUtils.isEmpty(sex)) {
                user.setSex(Integer.parseInt(sex));
            } else {
                user.setSex(-1);
            }
            return user;
        }
        return null;
    }

    /**
     * 获取用户头像信息
     */
    public Bitmap getUserAvatar(String account) {
        if (!isConnected() || TextUtils.isEmpty(account)) {
            return null;
        }
        Bitmap ic = null;
        try {
            if (!account.contains("@")) {
                account = account + "@" + SERVER_DOMAIN;
            }
            VCard vcard = VCardManager.getInstanceFor(xmppConnection).loadVCard(JidCreate.entityBareFrom(account));
            if (vcard == null || vcard.getAvatar() == null) {
                return null;
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    vcard.getAvatar());

            ic = BitmapFactory.decodeStream(bais);

        } catch (Exception e) {
            Logger.e(e);
        }
        return ic;
    }

/**
 * 发消息
 *
 * @param type    消息类型
 * @param subject 消息头
 * @param user    目标
 * @param body    消息
 * @throws Exception
 */
    public void sendUserMsg(Message.Type type, String subject,
                            String user, String body) throws Exception {

        ChatManager chatManager = ChatManager.getInstanceFor(xmppConnection);
        EntityBareJid targetUser = JidCreate.entityBareFrom(user);
        Chat chat = chatManager.chatWith(targetUser);
        Message msg = new Message();
        msg.setType(type);
        msg.setSubject(subject);
        msg.setBody(body);
        chat.send(msg);
    }

}
