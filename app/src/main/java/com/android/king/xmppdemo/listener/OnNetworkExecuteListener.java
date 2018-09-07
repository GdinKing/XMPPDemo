package com.android.king.xmppdemo.listener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月05日 16:56分
 * @since 2018-09-05
 * @author king
 */
public interface OnNetworkExecuteListener {

    void onExecute() throws Exception;

    void onFinish(Exception e);
}
