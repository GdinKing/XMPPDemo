package com.android.king.xmppdemo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.king.xmppdemo.R;


/**
 * 功能面板
 */
public class PanelFragment extends Fragment {

    public static PanelFragment newInstance() {
        PanelFragment fragment = new PanelFragment();
        return fragment;
    }

    private OnPanelItemClickListener panelItemClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pannel, container, false);

        LinearLayout containerView = v.findViewById(R.id.container);
        for (int i = 0; i < containerView.getChildCount(); i++) {
            final int index = i;
            containerView.getChildAt(index).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (panelItemClickListener != null) {
                        panelItemClickListener.onPanelItemClick(index);
                    }
                }
            });
        }
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPanelItemClickListener) {
            panelItemClickListener = (OnPanelItemClickListener) context;
        } else {
            throw new IllegalArgumentException(context + " must implement interface " + OnPanelItemClickListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        panelItemClickListener = null;
        super.onDetach();
    }

    public interface OnPanelItemClickListener {
        void onPanelItemClick(int position);
    }
}
