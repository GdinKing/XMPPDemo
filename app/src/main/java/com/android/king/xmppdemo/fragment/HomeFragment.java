package com.android.king.xmppdemo.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.db.SQLiteHelper;
import com.android.king.xmppdemo.entity.Apply;
import com.android.king.xmppdemo.event.FriendEvent;
import com.android.king.xmppdemo.event.LoginEvent;
import com.android.king.xmppdemo.listener.OnExecuteCallback;
import com.android.king.xmppdemo.net.AsyncExecutor;
import com.android.king.xmppdemo.ui.LoginActivity;
import com.android.king.xmppdemo.util.CommonUtil;
import com.android.king.xmppdemo.util.Logger;
import com.android.king.xmppdemo.util.SPUtil;
import com.android.king.xmppdemo.xmpp.XMPPHelper;
import com.android.king.xmppdemo.xmpp.XMPPService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import me.yokeyword.fragmentation.SupportFragment;
import q.rorbin.badgeview.QBadgeView;


/**
 * 微聊
 */
public class HomeFragment extends SupportFragment implements View.OnClickListener {

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    private SupportFragment[] mFragments = new SupportFragment[4];

    private TextView tabFriends;
    private TextView tabMessage;
    private TextView tabFind;
    private TextView tabMy;
    private TextView tvTitle;

    private View lastSelect;

    private ImageView ivSearch;
    private ImageView ivAdd;

    private PopupWindow popupWindow;

    private int prePosition = 0;

    private View rootView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SupportFragment firstFragment = findChildFragment(ChatFragment.class);
        if (firstFragment == null) {
            mFragments[0] = ChatFragment.newInstance();
            mFragments[1] = FriendsFragment.newInstance();
            mFragments[2] = FindFragment.newInstance();
            mFragments[3] = MyFragment.newInstance();
            loadMultipleRootFragment(R.id.home_container, 0, mFragments[0], mFragments[1], mFragments[2], mFragments[3]);
        } else {
            mFragments[0] = firstFragment;
            mFragments[1] = findChildFragment(FriendsFragment.class);
            mFragments[2] = findChildFragment(FindFragment.class);
            mFragments[3] = findChildFragment(MyFragment.class);
        }
        prePosition = 0;

    }

    private String getCurrentLogin() {
        return SPUtil.getString(getActivity(), AppConstants.SP_KEY_LOGIN_ACCOUNT);
    }

    @Override
    public void onStart() {
        super.onStart();
        hideSoftInput();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        tvTitle = rootView.findViewById(R.id.tv_title);
        ivAdd = rootView.findViewById(R.id.iv_add);
        ivSearch = rootView.findViewById(R.id.iv_search);
        ivAdd.setOnClickListener(this);
        ivSearch.setOnClickListener(this);

        tabFriends = rootView.findViewById(R.id.tv_friends);
        tabMessage = rootView.findViewById(R.id.tv_message);
        tabFind = rootView.findViewById(R.id.tv_find);
        tabMy = rootView.findViewById(R.id.tv_my);

        tabMessage.setOnClickListener(this);
        tabFriends.setOnClickListener(this);
        tabFind.setOnClickListener(this);
        tabMy.setOnClickListener(this);
        tvTitle.setText("微聊");
        tabMessage.setSelected(true);
        lastSelect = tabMessage;

        initData();
        return rootView;
    }

    private void initData() {
        //判断是否登录过
        boolean isLogin = SPUtil.getBoolean(getActivity(), AppConstants.SP_KEY_LOGIN_STATUS, false);
        if (isLogin) {
            if (!XMPPHelper.getInstance().isLogin()) {//账号已经登录过，但xmpp未登录
                Logger.i("未登录");
                final String account = SPUtil.getString(getActivity(), AppConstants.SP_KEY_LOGIN_ACCOUNT);
                final String password = SPUtil.getString(getActivity(), AppConstants.SP_KEY_LOGIN_PASSWORD);
                AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
                    @Override
                    public Object onExecute() throws Exception {
                        XMPPHelper.getInstance().login(account, password);
                        return null;
                    }

                    @Override
                    public void onFinish(Object result, Exception e) {
                        if (e != null) {
                            Logger.e(e);
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                            getActivity().finish();
                            return;
                        }
                        addListener();
                    }
                });
            } else {
                addListener();
            }
        }
    }

    /**
     * 监听器
     */
    private void addListener() {
        Intent intent = new Intent(getActivity(), XMPPService.class);
        getActivity().startService(intent);
        XMPPHelper.getInstance().addListeners();
        EventBus.getDefault().post(new LoginEvent(true));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add:
                showAddMenu();
                break;
            case R.id.iv_search:

                break;
            case R.id.tv_message:
                lastSelect.setSelected(false);
                tabMessage.setSelected(true);
                lastSelect = tabMessage;
                showHideFragment(mFragments[0], mFragments[prePosition]);
                prePosition = 0;
                break;
            case R.id.tv_friends:
                lastSelect.setSelected(false);
                tabFriends.setSelected(true);
                lastSelect = tabFriends;
                showHideFragment(mFragments[1], mFragments[prePosition]);
                prePosition = 1;
                break;
            case R.id.tv_find:
                lastSelect.setSelected(false);
                tabFind.setSelected(true);
                lastSelect = tabFind;
                showHideFragment(mFragments[2], mFragments[prePosition]);
                prePosition = 2;
                break;
            case R.id.tv_my:
                lastSelect.setSelected(false);
                tabMy.setSelected(true);
                lastSelect = tabMy;
                showHideFragment(mFragments[3], mFragments[prePosition]);
                prePosition = 3;
                break;
        }
    }

    /**
     * 好友申请/状态
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendEvent(FriendEvent event) {
        String from = event.from;
        switch (event.status) {
            case AppConstants.StanzaStatus.SUBSCRIBE:
                CommonUtil.showApplyNotify(getActivity(), from.split("@")[0] + "请求加你为好友");
                checkApplyExist(from);
                ((FriendsFragment) mFragments[1]).checkApply();
                break;
            case AppConstants.StanzaStatus.SUBSCRIBED:
                Toast.makeText(getActivity(), from.split("@")[0] + "通过了你的好友申请！", Toast.LENGTH_LONG).show();
                ((FriendsFragment) mFragments[1]).loadData();
                break;
            case AppConstants.StanzaStatus.UNSUBSCRIBE:
                Logger.i(from.split("@")[0] + "拒绝了你的好友申请！");
                break;
            case AppConstants.StanzaStatus.UNAVAILABLE:

                if (from.split("@")[0].equals(getCurrentLogin())) {
                    AsyncExecutor.getInstance().execute(new OnExecuteCallback() {
                        @Override
                        public Object onExecute() throws Exception {

                            XMPPHelper.getInstance().reconnect();
                            return null;
                        }

                        @Override
                        public void onFinish(Object result, Exception e) {
                            if (e != null) {
                                return;
                            }
                            XMPPHelper.getInstance().addListeners();
                        }
                    });
                }
                break;
        }
    }


    /**
     * 查询好友申请是否已存在数据库表中
     *
     * @param from
     */
    private void checkApplyExist(String from) {
        Cursor cursor = SQLiteHelper.getInstance(getActivity()).query(AppConstants.TABLE_APPLY, new String[]{"fromUser", "isAgree"}, "fromUser=?", new String[]{from}, null, null, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex("isAgree"));
                if (status != Apply.STATUS_IGNORE && status != Apply.STATUS_AGREED) {
                    ContentValues cv = new ContentValues();
                    cv.put("isAgree", Apply.STATUS_UNAGREE);
                    SQLiteHelper.getInstance(getActivity()).update(AppConstants.TABLE_APPLY, cv, "fromUser=?", new String[]{from});
                }
            } else {
                ContentValues cv = new ContentValues();
                cv.put("fromUser", from);
                cv.put("name", from.split("@")[0]);
                cv.put("isAgree", Apply.STATUS_UNAGREE);
                SQLiteHelper.getInstance(getActivity()).insert(AppConstants.TABLE_APPLY, cv);
            }
        }
    }


    private void showAddMenu() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popup_layout_add_menu, null);
        TextView tvAddFrient = view.findViewById(R.id.tv_add_friend);
        TextView tvMultiChat = view.findViewById(R.id.tv_multi_chat);
        TextView tvScan = view.findViewById(R.id.tv_scan);

        tvAddFrient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                start(SearchUserFragment.newInstance());
            }
        });

        tvMultiChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        tvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        if (popupWindow == null) {
            popupWindow = new PopupWindow(view);
            popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
        }
        popupWindow.showAsDropDown(ivAdd, 0, 10);
    }

    @Override
    public boolean onBackPressedSupport() {
        getActivity().moveTaskToBack(true);
        return true;
    }

    public void startFragment(SupportFragment fragment) {
        start(fragment);
    }

    private QBadgeView friendBadge;
    private QBadgeView messageBadge;

    public void setFriendBadge(int count) {

        if (friendBadge != null) {
            friendBadge.hide(false);
        }
        if (count <= 0) {
            return;
        }
        friendBadge = new QBadgeView(getActivity());
        friendBadge.bindTarget(tabFriends)
                .setBadgeGravity(Gravity.TOP | Gravity.END)
                .setGravityOffset(10, 0, true)
                .setBadgeNumber(count);
    }


    public void setMessageBadge(int count) {
        if (messageBadge != null) {
            messageBadge.hide(false);
        }
        if (count <= 0) {
            return;
        }
        messageBadge = new QBadgeView(getActivity());
        messageBadge.bindTarget(tabMessage)
                .setBadgeGravity(Gravity.TOP | Gravity.END)
                .setGravityOffset(10, 0, true)
                .setBadgeNumber(count);
    }


}
