package com.android.king.xmppdemo.fragment;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.ListView;
import android.widget.TextView;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.adapter.NewApplyAdapter;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.db.SQLiteHelper;
import com.android.king.xmppdemo.entity.Apply;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.event.AgreeEvent;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


/**
 * 新的好朋友申请
 */
public class NewApplyFragment extends BaseFragment {

    public static NewApplyFragment newInstance() {
        NewApplyFragment fragment = new NewApplyFragment();
        return fragment;
    }


    private NewApplyAdapter mAdapter;

    private ListView lvUser;
    private TextView tvEmpty;
    private List<Apply> dataList = new ArrayList<>();

    @Override
    protected int getContentView() {
        return R.layout.fragment_new_apply;
    }

    @Override
    protected void initView() {
        setTitle("新的朋友");
        tvEmpty = rootView.findViewById(R.id.tv_empty);
        lvUser = rootView.findViewById(R.id.lv_users);
    }

    @Override
    protected void initData() {
        mAdapter = new NewApplyAdapter(getActivity(), dataList);
        lvUser.setAdapter(mAdapter);
        lvUser.setEmptyView(tvEmpty);
        mAdapter.setAgreeClickListener(new NewApplyAdapter.OnAgreeClickListener() {
            @Override
            public void onAgree(int position) {
                Apply apply = dataList.get(position);
                acceptRejectApply(apply.getUser().getAccount(), 0);
            }

            @Override
            public void onIgnore(int position) {
                Apply apply = dataList.get(position);
                acceptRejectApply(apply.getUser().getAccount(), 1);
            }
        });
        loadData();
    }

    private void loadData() {
        dataList.clear();
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback<Void>() {
            @Override
            public Void onExecute() throws Exception {
                Cursor cursor = SQLiteHelper.getInstance(getActivity()).query(AppConstants.TABLE_APPLY, null, null, null, null, null, null);
                if (null != cursor) {
                    while (cursor.moveToNext()) {
                        String from = cursor.getString(cursor.getColumnIndex("fromUser"));
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        int isAgree = cursor.getInt(cursor.getColumnIndex("isAgree"));

                        User user = new User();
                        user.setName(name);
                        user.setAccount(from);

                        Apply apply = new Apply(user, isAgree);

                        dataList.add(apply);
                    }
                }
                cursor.close();
                return null;
            }

            @Override
            public void onFinish(Void result, Exception e) {
                if (e != null) {
                    showToast("获取数据失败");
                    Logger.e(e);
                    return;
                }
                mAdapter.refreshData(dataList);
            }
        });

    }


    private void acceptRejectApply(final String from, final int flag) {
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback<Integer>() {
            @Override
            public Integer onExecute() throws Exception {
                if (flag == 0) {
                    XMPPHelper.getInstance().accept(from);
                } else {
                    XMPPHelper.getInstance().refuse(from);
                }
                return flag;
            }

            @Override
            public void onFinish(Integer result, Exception e) {

                if (e != null) {
                    Logger.e(e);
                    showToast("请求失败");
                    return;
                }
                if (result == 0) {
                    ContentValues cv = new ContentValues();
                    cv.put("isAgree", Apply.STATUS_AGREED);
                    SQLiteHelper.getInstance(getActivity()).update(AppConstants.TABLE_APPLY, cv, "fromUser=?", new String[]{from});

                } else {
                    ContentValues cv = new ContentValues();
                    cv.put("isAgree", Apply.STATUS_IGNORE);
                    SQLiteHelper.getInstance(getActivity()).update(AppConstants.TABLE_APPLY, cv, "fromUser=?", new String[]{from});
                }
                EventBus.getDefault().post(new AgreeEvent());
                loadData();
            }
        });
    }


    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }
}
