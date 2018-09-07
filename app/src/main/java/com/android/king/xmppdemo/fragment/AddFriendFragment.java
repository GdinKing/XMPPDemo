package com.android.king.xmppdemo.fragment;

import android.annotation.SuppressLint;
import android.king.xmppdemo.R;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.king.xmppdemo.adapter.UserAdapter;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class AddFriendFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    public static AddFriendFragment newInstance() {
        AddFriendFragment fragment = new AddFriendFragment();
        return fragment;
    }

    private EditText etAccount;
    private Button btnAdd;

    private UserAdapter userAdapter;

    private ListView lvUser;
    private TextView tvEmpty;
    private List<User> dataList = new ArrayList<>();

    @Override
    protected int getContentView() {
        return R.layout.fragment_add_friend;
    }

    @Override
    protected void initView() {
        setTitle("添加好友");
        etAccount = rootView.findViewById(R.id.et_account);
        btnAdd = rootView.findViewById(R.id.btn_add);
        tvEmpty = rootView.findViewById(R.id.tv_empty);
        lvUser = rootView.findViewById(R.id.lv_users);
        lvUser.setOnItemClickListener(this);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString().trim();
                addFriend(account);
//                searchUser(account);
            }
        });
    }

    @Override
    protected void initData() {
        userAdapter = new UserAdapter(getActivity(), dataList);
        lvUser.setAdapter(userAdapter);
        lvUser.setEmptyView(tvEmpty);
    }


    private void searchUser(final String account) {
        showProgress("搜索中");
        new Thread() {
            @Override
            public void run() {
                try {
                    ProviderManager.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

                    AbstractXMPPConnection connection = XMPPHelper.getInstance().getConnection();
                    if (connection == null || !connection.isConnected()) {
                        mHandler.sendEmptyMessage(403);
                        return;
                    }
                    DomainBareJid serverDomain = JidCreate.domainBareFrom("search." + XMPPHelper.SERVER_DOMAIN);

                    UserSearchManager usm = new UserSearchManager(connection);
                    Form answerForm = usm.getSearchForm(serverDomain).createAnswerForm();
                    if (answerForm == null) {

                        mHandler.sendEmptyMessage(404);
                        return;
                    }
                    answerForm.setAnswer("Username", true);
                    answerForm.setAnswer("Name", true);
                    answerForm.setAnswer("search", account);
                    ReportedData data = usm.getSearchResults(answerForm, serverDomain);

                    List<ReportedData.Row> it = data.getRows();
                    for (ReportedData.Row row : it) {
                        Logger.i("数据:" + row.toString());
                        User user = new User();
                        user.setAccount(row.getValues("Username").get(0).toString());
                        user.setName(row.getValues("Name").get(0).toString());
                        dataList.add(user);
                        //若存在，则有返回,UserName一定非空，其他两个若是有设，一定非空
                    }
                    mHandler.sendEmptyMessage(100);
                } catch (Exception e) {
                    Logger.e(e);
                    mHandler.sendEmptyMessage(404);
                }
            }
        }.start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismissProgress();
            if (msg.what == 100) {
                userAdapter.refreshData(dataList);
            } else if (msg.what == 403) {
                Toast.makeText(getActivity(), "连接断开", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 404) {
                Toast.makeText(getActivity(), "连接服务器失败", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 101) {
                boolean flag = (boolean) msg.obj;
                if (flag) {
                    Toast.makeText(getActivity(), "发送申请成功", Toast.LENGTH_SHORT).show();
                    pop();
                } else {
                    Toast.makeText(getActivity(), "发送申请失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (dataList != null) {
            User user = dataList.get(position);
            addFriend(user.getAccount());
        }
    }

    private void addFriend(final String account) {
        new Thread() {
            @Override
            public void run() {
                try {
                    boolean flag = XMPPHelper.getInstance().applyFriend(account);
                    mHandler.obtainMessage(101, flag).sendToTarget();
                } catch (Exception e) {
                    mHandler.sendEmptyMessage(404);
                }
            }
        }.start();
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }
}
