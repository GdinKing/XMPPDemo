package com.android.king.xmppdemo.xmpp;

import android.text.TextUtils;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.event.FriendEvent;
import com.android.king.xmppdemo.listener.IncomingMsgListener;
import com.android.king.xmppdemo.listener.OnInvitationListener;
import com.android.king.xmppdemo.util.FileUtil;
import com.android.king.xmppdemo.util.Logger;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
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
    public static final int SERVER_PORT = 5222;

    private static XMPPHelper mInstance = null;

    private AbstractXMPPConnection xmppConnection = null;

    private IncomingMsgListener incomingListener = null;
    private OnInvitationListener invitationListener = null;

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
            builder.setPort(SERVER_PORT);
            builder.setSendPresence(true);//若接收不了离线消息，设置为false
            builder.setConnectTimeout(15000);
            builder.setCompressionEnabled(true);
            builder.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled);
            builder.setDebuggerEnabled(false);
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
//        if (isConnected()) {
//            xmppConnection.disconnect();
//            Thread.sleep(1000);
//        }
        xmppConnection.connect();

        SASLAuthentication.blacklistSASLMechanism("ANONYMOUS");
        SASLAuthentication.blacklistSASLMechanism("CRAM-MD5");
        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
        SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1-PLUS");

        // 自动回复回执方法，如果对方的消息要求回执
        ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
        ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceiptRequest.Provider());
        DeliveryReceiptManager.getInstanceFor(xmppConnection).autoAddDeliveryReceiptRequests();

        ReconnectionManager manager = ReconnectionManager.getInstanceFor(xmppConnection);
        manager.setFixedDelay(0);//延迟0秒重连
        manager.enableAutomaticReconnection();
        xmppConnection.login(username, password, Resourcepart.from(resource));
        PingManager.getInstanceFor(xmppConnection).setPingInterval(20);//心跳
    }

    /**
     * 重连
     *
     * @throws Exception
     */
    public void reconnect() throws Exception {
        if (isConnected()) {
            return;
        }
        xmppConnection.connect();
        if (!xmppConnection.isAuthenticated()) {
            xmppConnection.login();//之前登陆过了，smack会设置账号密码，可看login()的源码
        }

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
                case AppConstants.StanzaStatus.AVAILABLE:
                    presence = new Presence(Presence.Type.available);//在线
                    break;
                case AppConstants.StanzaStatus.UNAVAILABLE:
                    presence = new Presence(Presence.Type.unavailable);//离线
                    break;
                default:
                    presence = new Presence(Presence.Type.available);//在线
                    break;
            }
//            presence.setTo(userName); 这里可以设置对某人显示离线（即隐身）
            if (isLogin()) {
                xmppConnection.sendStanza(presence);
            }
        } catch (Exception e) {
            Logger.e(e);
        }
    }


    /**
     * 修改用户头像
     *
     * @param f
     */
    public void changeImage(File f) throws XMPPException, IOException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        VCard vcard = VCardManager.getInstanceFor(xmppConnection).loadVCard();
        byte[] bytes = FileUtil.getFileBytes(f);
        String encodedImage = Base64.encodeToString(bytes);
        String mineType = "image/png";
        if (f.getAbsolutePath().endsWith("jpg") || f.getAbsolutePath().endsWith("jpeg")) {
            mineType = "image/jpeg";
        }
        vcard.setAvatar(encodedImage, mineType);

        vcard.setField("PHOTO", "<TYPE>" + mineType + "</TYPE><BINVAL>"
                + encodedImage + "</BINVAL>", true);

        VCardManager.getInstanceFor(xmppConnection).saveVCard(vcard);
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
        removeMessageListener();
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
        vCard.setNickName(user.getNickName());
        vCard.setFirstName(user.getName());
        vCard.setEmailWork(user.getEmail());
        vCard.setJabberId(user.getAccount());
        vCard.setField("sex", String.valueOf(user.getSex()));
        vCard.setField("sign", user.getSign());
        vCard.setField("avatar", user.getAvatar());
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
                account = account + "@" + xmppConnection.getServiceName();
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
                account = account + "@" + xmppConnection.getServiceName();
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
                account = account + "@" + xmppConnection.getServiceName();
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
     * 真要搭建即时通讯app，是不建议这种获取好友的方式的，因为数据不全而且查询麻烦，自己写个Web服务连接数据库进行查询会更适合
     * 或者开发个openfire插件来管理好友信息
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
                user.setAvatar(getVcardAvatar(entry.getJid().toString()));
                userList.add(user);
            }
        }
        return userList;
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
                account = account + "@" + xmppConnection.getServiceName();
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
            user.setAvatar(getVcardAvatar(account));
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
     * 获取VCard中用户的头像并存储到本地缓存
     */
    public String getVcardAvatar(String account) {
        if (!account.contains("@")) {
            account = account + "@" + xmppConnection.getServiceName();
        }
        if (FileUtil.isAvatarExist(account)) {
            return FileUtil.getAvatarCache(account);
        }
        if (!isConnected() || TextUtils.isEmpty(account)) {
            return null;
        }
        try {

            VCard vcard = VCardManager.getInstanceFor(xmppConnection).loadVCard(JidCreate.entityBareFrom(account));
            if (vcard == null || vcard.getAvatar() == null) {

                Logger.i("获取头像为空");
                return null;
            }
            return FileUtil.saveAvatarToFile(vcard.getAvatar(), account + ".png");

        } catch (Exception e) {
            Logger.e(e);
        }
        return null;
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
        if (!isLogin()) {
            throw new RuntimeException("连接错误");
        }
        ChatManager chatManager = ChatManager.getInstanceFor(xmppConnection);
        EntityBareJid targetUser = JidCreate.entityBareFrom(user);
        Chat chat = chatManager.chatWith(targetUser);
        Message msg = new Message();
        msg.setType(type);
        msg.setSubject(subject);
        msg.setBody(body);
        chat.send(msg);
    }


    /**
     * 添加消息监听
     */
    public void addMessageListener() {
        if (incomingListener == null) {
            incomingListener = new IncomingMsgListener();
        }
        ChatManager.getInstanceFor(xmppConnection).addIncomingListener(incomingListener);
    }


    /**
     * 移除监听
     */
    public void removeMessageListener() {
        if (incomingListener == null) {
            return;
        }
        ChatManager.getInstanceFor(xmppConnection).removeIncomingListener(incomingListener);
    }

    /**
     * 响应回复监听
     */
    public void addStanzaListener() {
        //条件过滤
        StanzaFilter filter = new AndFilter(new StanzaTypeFilter(Presence.class));
        StanzaListener listener = new StanzaListener() {
            @Override
            public void processStanza(Stanza stanza) {
                Logger.i(stanza.toString());
                if (stanza instanceof Presence) {
                    Presence p = (Presence) stanza;
                    Logger.i("收到回复：" + p.getFrom() + "--" + p.getType());
                    String from = p.getFrom().toString();
                    String status = p.getType().toString();
                    EventBus.getDefault().post(new FriendEvent(from, status));
                }
            }

        };

        xmppConnection.addAsyncStanzaListener(listener, filter);
    }


    public List<Message> getOfflineMessage() throws Exception {
        OfflineMessageManager offlineManager = new OfflineMessageManager(xmppConnection);
        //获取离线消息
        List<Message> messageList = offlineManager.getMessages();

        //特别说明，这条代码的意思是获取离线消息的数量，我也不知道为啥只有加了这句才可以真正删除离线记录，
        //否则就一直删不掉，老重复接收重复的离线消息记录
        offlineManager.getMessageCount();
        //获取后删除离线消息记录
        offlineManager.deleteMessages();

        //设置在线，只有设置了在线状态，才可以监听在线消息，否则监听都无效
        Presence presence = new Presence(Presence.Type.available);
        xmppConnection.sendStanza(presence);
        return messageList;
    }

    /**
     * 获取服务器上的所有群组
     */
    public List<HostedRoom> getHostedRoom() throws XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException, MultiUserChatException.NotAMucServiceException {
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(xmppConnection);

        List<DomainBareJid> serviceNames = manager.getXMPPServiceDomains();
        for (int i = 0; i < serviceNames.size(); i++) {
            return manager.getHostedRooms(serviceNames.get(i));
        }
        return null;
    }

    /**
     * 获取用户的的所有群组
     */
    public List<MultiUserChat> getJoinRoom(String user) throws XmppStringprepException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        if (!user.contains("@")) {
            user = user + "@" + xmppConnection.getServiceName();
        }

        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(xmppConnection);

        List<EntityBareJid> rooms = manager.getJoinedRooms(JidCreate.entityBareFrom(user));
        List<MultiUserChat> multiChatList = new ArrayList<>();
        for (EntityBareJid jid : rooms) {
            MultiUserChat chat = manager.getMultiUserChat(jid);
            multiChatList.add(chat);
        }
        return multiChatList;

    }

    /**
     * 加入一个群聊聊天室
     *
     * @param groupId  聊天室ip 格式为>>群组名称@conference.ip
     * @param nickName 用户在聊天室中的昵称
     * @param password 聊天室密码 没有密码则传""
     * @return
     */
    public MultiUserChat joinMultiChat(String groupId, String nickName, String password) throws Exception {

        // 使用XMPPConnection创建一个MultiUserChat窗口
        MultiUserChat muc = MultiUserChatManager.getInstanceFor(xmppConnection).getMultiUserChat(JidCreate.entityBareFrom(groupId));

        MucEnterConfiguration.Builder builder = muc.getEnterConfigurationBuilder(Resourcepart.from(nickName));
        //只获取最后10条历史记录
        builder.requestMaxCharsHistory(10);
        builder.withPassword(password);
        MucEnterConfiguration mucEnterConfiguration = builder.build();
        //加入群
        muc.join(mucEnterConfiguration);
        return muc;


//        if ("XMPPError: not-authorized - auth".equals(e.getMessage())) {
//            //需要密码加入
//        }
    }

    /**
     * 创建群聊聊天室
     *
     * @param roomName 聊天室名字
     * @param nickName 创建者在聊天室中的昵称
     * @param password 聊天室密码
     * @return
     */
    public MultiUserChat createChatRoom(String roomName, String nickName, String password, List<User> users) {
        MultiUserChat muc;
        try {
            // 创建一个MultiUserChat
            muc = MultiUserChatManager.getInstanceFor(xmppConnection).getMultiUserChat(JidCreate.entityBareFrom(roomName + "@conference." + xmppConnection.getServiceName()));
            // 创建聊天室
            MultiUserChat.MucCreateConfigFormHandle handle = muc.create(Resourcepart.from(nickName));
            if (handle != null) {
                // 获得聊天室的配置表单
                Form form = muc.getConfigurationForm();
                // 根据原始表单创建一个要提交的新表单。
                Form submitForm = form.createAnswerForm();
                // 向要提交的表单添加默认答复
                List<FormField> fields = form.getFields();
                for (int i = 0; fields != null && i < fields.size(); i++) {
                    if (FormField.Type.hidden != fields.get(i).getType() &&
                            fields.get(i).getVariable() != null) {
                        // 设置默认值作为答复
                        submitForm.setDefaultAnswer(fields.get(i).getVariable());
                    }
                }
                // 设置聊天室的新拥有者
                List owners = new ArrayList();
                owners.add(xmppConnection.getUser());// 用户JID
                if (users != null && !users.isEmpty()) {
                    for (int i = 0; i < users.size(); i++) {  //添加群成员,用户jid格式和之前一样 用户名@openfire服务器名称
                        EntityBareJid userJid = JidCreate.entityBareFrom(users.get(i).getAccount());
                        owners.add(userJid.toString());
                    }
                }

                submitForm.setAnswer("muc#roomconfig_roomowners", owners);
                // 设置聊天室是持久聊天室，即将要被保存下来
                submitForm.setAnswer("muc#roomconfig_persistentroom", true);
                // 房间仅对成员开放
                submitForm.setAnswer("muc#roomconfig_membersonly", false);
                // 允许占有者邀请其他人
                submitForm.setAnswer("muc#roomconfig_allowinvites", true);
                if (password != null && password.length() != 0) {
                    // 进入是否需要密码
                    submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
                    // 设置进入密码
                    submitForm.setAnswer("muc#roomconfig_roomsecret", password);
                }
                // 能够发现占有者真实 JID 的角色
                // submitForm.setAnswer("muc#roomconfig_whois", "anyone");
                // 登录房间对话
                submitForm.setAnswer("muc#roomconfig_enablelogging", true);
                // 仅允许注册的昵称登录
                submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
                // 允许使用者修改昵称
                submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
                // 允许用户注册房间
                submitForm.setAnswer("x-muc#roomconfig_registration", false);
                // 发送已完成的表单（有默认值）到服务器来配置聊天室
                muc.sendConfigurationForm(submitForm);

            } else {
                //error
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return muc;
    }

    /**
     * 退出群聊
     *
     * @param groupName
     * @throws XmppStringprepException
     */
    public void quitRoom(String groupName) throws Exception {
        String jid = groupName + "@conference." + xmppConnection.getServiceName();
        EntityBareJid groupJid = JidCreate.entityBareFrom(jid);

        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(xmppConnection);
        MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(groupJid);
        //退出群
        multiUserChat.leave();
    }

    /**
     * 发送群聊消息
     *
     * @param groupName
     * @param body
     */
    public void sendChatGroupMessage(String groupName, String body) throws Exception {
        //拼凑jid
        String jid = groupName + "@conference." + xmppConnection.getServiceName();
        //创建jid实体
        EntityBareJid groupJid = JidCreate.entityBareFrom(jid);
        //群管理对象
        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(xmppConnection);
        MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(groupJid);
        //发送信息
        multiUserChat.sendMessage(body);

    }

    /**
     * 监听群聊申请
     */
    public void addInvitationListener() {
        if (invitationListener == null) {
            invitationListener = new OnInvitationListener();
        }
        MultiUserChatManager.getInstanceFor(xmppConnection).addInvitationListener(invitationListener);
    }

    /**
     * 群聊消息监听
     *
     * @param group
     * @throws XmppStringprepException
     */
    public void multiChatListener(String group) throws XmppStringprepException {
        if (!group.contains("@")) {
            group = group + "@" + SERVER_DOMAIN;
        }
        MultiUserChat multiUserChat = MultiUserChatManager.getInstanceFor(xmppConnection).getMultiUserChat(JidCreate.entityBareFrom(group));
        multiUserChat.addMessageListener(new MessageListener() {
            @Override
            public void processMessage(final Message message) {
                //当消息返回为空的时候，表示用户正在聊天窗口编辑信息并未发出消息
                if (!TextUtils.isEmpty(message.getBody())) {
                    //收到的消息
                    Logger.i(message.getBody());
                }
            }
        });
    }

    public void addListeners() {
        addMessageListener();
        addInvitationListener();
        addStanzaListener();
    }
}
