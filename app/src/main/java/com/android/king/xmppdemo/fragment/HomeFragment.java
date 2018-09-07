package com.android.king.xmppdemo.fragment;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SupportFragment firstFragment = findChildFragment(ChatFragment.class);
        if (firstFragment == null) {
            mFragments[0] = ChatFragment.newInstance();
            mFragments[1] = FriendsFragment.newInstance();
            mFragments[2] = MomentFragment.newInstance();
            mFragments[3] = MyFragment.newInstance();
            loadMultipleRootFragment(R.id.home_container, 0, mFragments[0], mFragments[1], mFragments[2], mFragments[3]);
        } else {
            mFragments[0] = firstFragment;
            mFragments[1] = findChildFragment(FriendsFragment.class);
            mFragments[2] = findChildFragment(MomentFragment.class);
            mFragments[3] = findChildFragment(MyFragment.class);
        }
        prePosition = 0;
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

}
