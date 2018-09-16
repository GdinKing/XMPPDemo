package com.android.king.xmppdemo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.util.Logger;
import com.king.scanlibrary.android.CaptureActivity;
import com.king.scanlibrary.bean.ZxingConfig;
import com.king.scanlibrary.common.Constant;

import me.yokeyword.fragmentation.SupportFragment;
import q.rorbin.badgeview.QBadgeView;


/**
 * 发现
 */
public class FindFragment extends SupportFragment implements View.OnClickListener {

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
        switch (view.getId()) {
            case R.id.tv_moment:

                break;
            case R.id.tv_scan:
                Intent intent = new Intent(getActivity(), CaptureActivity.class);
                ZxingConfig config = new ZxingConfig();
                config.setPlayBeep(true);//是否播放扫描声音 默认为true
                config.setShake(false);//是否震动  默认为true
                config.setDecodeBarCode(false);//是否扫描条形码 默认为true
                config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为淡蓝色
                config.setFrameLineColor(R.color.background);//设置扫描框边框颜色 默认无色
                config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                startActivityForResult(intent, 101);
                break;
            case R.id.tv_nearby:

                break;
            case R.id.tv_news:
                ((HomeFragment) getParentFragment()).startFragment(WebFragment.newInstance("资讯", AppConstants.INFO_URL));
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null) {

                String content = data.getStringExtra(Constant.CODED_CONTENT);
                operation(content);
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void operation(String content) {
        if (content.startsWith("http://") || content.startsWith("https://")) {
            ((HomeFragment) getParentFragment()).startFragment(WebFragment.newInstance("", content));
        }else{
            Logger.i(content);
        }

    }
}
