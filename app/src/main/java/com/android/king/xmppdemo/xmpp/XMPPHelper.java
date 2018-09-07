package com.android.king.xmppdemo.xmpp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.text.TextUtils;

import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.listener.IncomingMsgListener;
import com.android.king.xmppdemo.util.Logger;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.iqprivate.PrivateDataManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
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
import java.util.Set;

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
            ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());
            ProviderManager.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
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
            builder.setCompressionEnabled(true);
            builder.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled);
            builder.setDebuggerEnabled(false);

            //允许别人添加好友
            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

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
        if (xmppConnection == null) {
            openConnection();
        }
        if (!isConnected()) {
            xmppConnection.connect();
        }
        SASLAuthentication.blacklistSASLMechanism("ANONYMOUS");
        xmppConnection.login(username, password, Resourcepart.from(resource));
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
        if (xmppConnection != null) {
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
     * 添加好友
     *
     * @param account   用户账号
     * @param nickName  用户昵称
     * @param groupName 所属组名
     * @return
     */
    public boolean addFriend(String account, String nickName, String groupName) {
        if (xmppConnection != null && xmppConnection.isConnected()) {
            try {
                EntityBareJid userJid = JidCreate.entityBareFrom(account + "@" + SERVER_DOMAIN);
                Roster.getInstanceFor(xmppConnection).createEntry(userJid, nickName, new String[]{groupName});
                return true;
            } catch (Exception e) {
                Logger.e(e);
                return false;
            }
        }
        throw new NullPointerException("服务器连接失败，请先连接服务器");
    }

    /**
     * 拒绝好友申请
     *
     * @param userId 用户id
     */
    public void refuse(String userId) {
        try {
            Presence presence = new Presence(Presence.Type.unsubscribe);
            presence.setTo(JidCreate.domainBareFrom(userId));
            xmppConnection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收好友申请
     *
     * @param userId 用户id
     */
    public void accept(String userId) {

        try {
            Presence presence = new Presence(Presence.Type.subscribe);
            presence.setTo(JidCreate.domainBareFrom(userId));
            xmppConnection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取所有好友
     * 真要搭建即时通讯系统，是不建议这种获取好友的方式的，因为数据不全，自己写个Web服务连接数据库进行查询会更适合
     *
     * @return
     */
    public List<User> getAllFriends() throws SmackException.NotLoggedInException, InterruptedException, SmackException.NotConnectedException {
        List<User> userList = new ArrayList<>();
        if (isLogin()) {
            Roster roster = Roster.getInstanceFor(xmppConnection);
            Logger.i("哈哈哈啊哈哈哈后啊哈哈哈哈哈哈");
            if (!roster.isLoaded()) {
                roster.reloadAndWait();
            }
            Collection<RosterEntry> entries = roster.getEntries();
            Logger.i("好友："+entries.size());
            for (RosterEntry entry : entries) {
                User user = new User();
                user.setAccount(entry.getJid().toString());
                user.setName(entry.getName());
                user.setNickName(entry.getName());
                userList.add(user);
            }
        }else{
            Logger.i("未登录");
        }
        return userList;
    }

    /**
     * 获取用户的vcard信息
     *
     * @param user
     * @return
     * @throws XMPPException
     */
    public VCard getUserVCard(String user) throws XMPPException, XmppStringprepException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        if (isConnected() && !TextUtils.isEmpty(user)) {
            String jid = user + "@" + SERVER_DOMAIN;
            VCard vCard = VCardManager.getInstanceFor(xmppConnection).loadVCard(JidCreate.entityBareFrom(jid));
            return vCard;
        }
        return null;
    }

    public User getUserInfo(String account) throws XMPPException, XmppStringprepException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        if (isConnected() && !TextUtils.isEmpty(account)) {
            String jid = account + "@" + SERVER_DOMAIN;
            VCard vCard = VCardManager.getInstanceFor(xmppConnection).loadVCard(JidCreate.entityBareFrom(jid));

            User user = new User();
            user.setName(vCard.getFirstName());
            user.setNickName(vCard.getNickName());
            user.setAccount(account);
            user.setEmail(vCard.getEmailWork());
            user.setAvatar(getUserAvatar(account));
            return user;
        }
        return null;
    }

    /**
     * 获取用户头像信息
     */
    public Bitmap getUserAvatar(String user) {
        if (!isConnected() || TextUtils.isEmpty(user)) {
            return null;
        }
        Bitmap ic = null;
        try {
            Logger.i("获取用户头像信息: " + user);
            String jid = user + "@" + SERVER_DOMAIN;
            VCard vcard = VCardManager.getInstanceFor(xmppConnection).loadVCard(JidCreate.entityBareFrom(jid));
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
//    public void sendUserMsg(Message.Type type, String subject,
//                            String user, String body) throws Exception {
//        if (chatManager == null) {
//            chatManager = ChatManager.getInstanceFor(xmppConnection);
//            chatManager.addIncomingListener(incomingListener);
//        }
//        EntityBareJid groupJid = JidCreate.entityBareFrom(user);
//        Chat chat = chatManager.chatWith(groupJid);
//        Message msg = new Message();
//        msg.setType(type);
//        msg.setSubject(subject);
//        msg.setBody(body);
//        chat.send(msg);
//    }

}
