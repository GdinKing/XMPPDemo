package com.android.king.xmppdemo.fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.adapter.UserAdapter;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.FixedQueryXElement;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
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
public class SearchUserFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    public static SearchUserFragment newInstance() {
        SearchUserFragment fragment = new SearchUserFragment();
        return fragment;
    }

    private EditText etAccount;

    private UserAdapter userAdapter;

    private ListView lvUser;
    private TextView tvEmpty;
    private TextView tvMyAccount;
    private List<User> dataList = new ArrayList<>();

    @Override
    protected int getContentView() {
        return R.layout.fragment_search_user;
    }

    @Override
    protected void initView() {
        setTitle("添加好友");
        etAccount = rootView.findViewById(R.id.et_account);
        tvEmpty = rootView.findViewById(R.id.tv_empty);
        tvMyAccount = rootView.findViewById(R.id.tv_my_account);
        lvUser = rootView.findViewById(R.id.lv_users);
        lvUser.setOnItemClickListener(this);

        etAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.toString().length() > 0) {
                    searchUser(s.toString());
                }
            }
        });
    }

    @Override
    protected void initData() {
        userAdapter = new UserAdapter(getActivity(), dataList);
        lvUser.setAdapter(userAdapter);
        lvUser.setEmptyView(tvEmpty);
        tvMyAccount.setText(getString(R.string.str_my_account, getCurrentLogin()));
    }


    /**
     * 这里有个坑
     * https://stackoverflow.com/questions/50715347/use-smackusersearchmanager-cannot-search-user
     *
     * @param account
     */
    private void searchUser(final String account) {


        AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
            @Override
            public Object onExecute() throws Exception {
                dataList.clear();
                AbstractXMPPConnection connection = XMPPHelper.getInstance().getConnection();

                DomainBareJid serverDomain = JidCreate.domainBareFrom("search." + XMPPHelper.SERVER_DOMAIN);

                UserSearchManager usm = new UserSearchManager(connection);
                Form answerForm = usm.getSearchForm(serverDomain).createAnswerForm();
                if (answerForm == null) {
                   
                    return null;
                }
                answerForm.setAnswer("Username", true);
                answerForm.setAnswer("Name", true);
                answerForm.setAnswer("search", account);
                ReportedData data = usm.getSearchResults(answerForm, serverDomain);

                List<ReportedData.Row> it = data.getRows();
                Logger.i("数据：" + it.size());
                for (ReportedData.Row row : it) {
                    String userName = row.getValues("UserName").get(0);
                    String name = row.getValues("Name").get(0);
                    User user = new User();
                    user.setAccount(userName);
                    user.setName(!TextUtils.isEmpty(name) ? name : userName);
                    user.setNote(!TextUtils.isEmpty(name) ? name : userName);
                    dataList.add(user);
                    //若存在，则有返回,UserName一定非空，其他两个若是有设，一定非空
                }
                return null;
            }

            @Override
            public void onFinish(Object result, Exception e) {
                if (e != null) {
                    Logger.e(e);
                    showToast("连接服务器失败");
                    return;
                }
                userAdapter.setKeyWord(account);
                userAdapter.refreshData(dataList);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (dataList != null) {
            User user = dataList.get(position);
            start(UserFragment.newInstance(user.getAccount(), user.getNote()));
        }
    }


    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }
}
