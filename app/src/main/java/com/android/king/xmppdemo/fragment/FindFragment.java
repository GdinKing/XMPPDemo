package com.android.king.xmppdemo.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.king.xmppdemo.R;

import me.yokeyword.fragmentation.SupportFragment;
import q.rorbin.badgeview.QBadgeView;


/**
 * 发现
 */
public class FindFragment extends SupportFragment implements View.OnClickListener{

    public static FindFragment newInstance() {
        FindFragment fragment = new FindFragment();
        return fragment;
    }

    private View btnMoment;
    private View btnScan;
    private View btnNearby;
    private View btnNews;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_find, container, false);

        btnMoment = v.findViewById(R.id.tv_moment);
        btnScan = v.findViewById(R.id.tv_scan);
        btnNearby = v.findViewById(R.id.tv_nearby);
        btnNews = v.findViewById(R.id.tv_news);
        btnMoment.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        btnNearby.setOnClickListener(this);
        btnNews.setOnClickListener(this);

        new QBadgeView(getActivity()).bindTarget(btnMoment)
                .setBadgeTextSize(14.0f, true)
                .setBadgeGravity(Gravity.CENTER | Gravity.END)
                .setGravityOffset(10.0f, true)
                .setBadgePadding(8.0f, true)
                .setBadgeNumber(1);
        return v;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_moment:

                break;
            case R.id.tv_scan:

                break;
            case R.id.tv_nearby:

                break;
            case R.id.tv_news:

                break;

        }
    }
}
