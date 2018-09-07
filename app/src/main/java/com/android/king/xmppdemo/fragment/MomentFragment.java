package com.android.king.xmppdemo.fragment;

import android.king.xmppdemo.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.fragmentation.SupportFragment;


/**
 * 发现
 */
public class MomentFragment extends SupportFragment {

    public static MomentFragment newInstance() {
        MomentFragment fragment = new MomentFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_moment, container, false);
    }

}
