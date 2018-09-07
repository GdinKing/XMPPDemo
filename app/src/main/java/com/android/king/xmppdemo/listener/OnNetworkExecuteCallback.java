package com.android.king.xmppdemo.listener;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月05日 16:56分
 * @since 2018-09-05
 * @author king
 */
public interface OnNetworkExecuteCallback {

    void onExecute() throws Exception;

    void onFinish(Exception e);
}
