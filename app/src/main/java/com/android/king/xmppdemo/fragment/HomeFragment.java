package com.android.king.xmppdemo.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.king.xmppdemo.R;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.listener.OnNetworkExecuteCallback;
import com.android.king.xmppdemo.net.NetworkExecutor;
import com.android.king.xmppdemo.util.CommonUtil;
import com.android.king.xmppdemo.view.TipDialog;
import com.android.king.xmppdemo.xmpp.XMPPHelper;

import me.yokeyword.fragmentation.SupportFragment;


/**
 * 微聊
 */
public class HomeFragment extends SupportFragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    private SupportFragment[] mFragments = new SupportFragment[4];

    private RadioGroup rgBottom;
    private TextView tvTitle;

    private ImageView ivSearch;
    private ImageView ivAdd;

    private PopupWindow popupWindow;

    private int prePosition = 0;


    private boolean isOnTop = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        isOnTop = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isOnTop = false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SupportFragment firstFragment = findChildFragment(MessageFragment.class);
        if (firstFragment == null) {
            mFragments[0] = MessageFragment.newInstance();
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

        IntentFilter filter = new IntentFilter(AppConstants.ACTION_FRIEND);
        getActivity().registerReceiver(addFriendReceiver, filter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        tvTitle = v.findViewById(R.id.tv_title);
        ivAdd = v.findViewById(R.id.iv_add);
        ivSearch = v.findViewById(R.id.iv_search);
        ivAdd.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        rgBottom = v.findViewById(R.id.rg_bottom);
        rgBottom.setOnCheckedChangeListener(this);
        tvTitle.setText("微聊");
        return v;
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_chat:
                showHideFragment(mFragments[0], mFragments[prePosition]);
                prePosition = 0;
                break;
            case R.id.rb_friends:
                showHideFragment(mFragments[1], mFragments[prePosition]);
                prePosition = 1;
                break;
            case R.id.rb_moment:
                showHideFragment(mFragments[2], mFragments[prePosition]);
                prePosition = 2;
                break;
            case R.id.rb_my:
                showHideFragment(mFragments[3], mFragments[prePosition]);
                ((MyFragment) mFragments[3]).loadData();
                prePosition = 3;
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add:
                showAddMenu();
                break;
            case R.id.iv_search:

                break;

        }
    }

    private String lastFrom;
    /**
     * 添加好友监听
     */
    private BroadcastReceiver addFriendReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int flag = intent.getIntExtra(AppConstants.INTENT_KEY_ADD_FRIEND, -1);
            String from = intent.getStringExtra(AppConstants.INTENT_KEY_ADD_FRIEND_FROM);
            if (flag == AppConstants.STATUS_ADD_FRIEND_OK) {
                Toast.makeText(getActivity(), from.split("@")[0] + "通过了你的好友申请！", Toast.LENGTH_LONG).show();
                ((FriendsFragment) mFragments[1]).loadData();
            } else if (flag == AppConstants.STATUS_ADD_FRIEND_RECEIVE) {
                if (isOnTop) {
                    showApplyDialog(from);
                } else {
                    CommonUtil.showNotify(getActivity(), from.split("@")[0] + "请求加你为好友");
                }
                if(!lastFrom.equals(from)) {
                    ((FriendsFragment) mFragments[1]).addFriendBadge();
                }
                lastFrom = from;
            } else if (flag == AppConstants.STATUS_ADD_FRIEND_REJECJ) {
                Toast.makeText(getActivity(), from.split("@")[0] + "拒绝了你的好友申请！", Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * 弹出请求添加好友对话框
     *
     * @param from
     */
    private void showApplyDialog(final String from) {

        final TipDialog dialog = new TipDialog(getActivity());
        dialog.setNegativeText("拒绝");
        dialog.setPositiveText("接受");
        dialog.setTipMessage(from.split("@")[0] + "请求加你为好友");
        dialog.setOnTipClickListener(new TipDialog.OnTipClickListener() {
            @Override
            public void onPositiveClick() {
                dialog.dismiss();
                acceptRejectApply(from, 0);

            }

            @Override
            public void onNegativeClick() {
                dialog.dismiss();
                acceptRejectApply(from, 1);
            }
        });
        dialog.show();

    }

    private void acceptRejectApply(final String from, final int flag) {
        NetworkExecutor.getInstance().execute(new OnNetworkExecuteCallback() {
            @Override
            public void onExecute() throws Exception {
                if (flag == 0) {
                    XMPPHelper.getInstance().accept(from);
                } else {
                    XMPPHelper.getInstance().refuse(from);
                }
            }

            @Override
            public void onFinish(Exception e) {
                ((FriendsFragment) mFragments[1]).loadData();
                ((FriendsFragment) mFragments[1]).hideBadge();
            }
        });
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
                start(AddFriendFragment.newInstance());
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

    @Override
    public void onDetach() {
        getActivity().unregisterReceiver(addFriendReceiver);
        super.onDetach();
    }

}
