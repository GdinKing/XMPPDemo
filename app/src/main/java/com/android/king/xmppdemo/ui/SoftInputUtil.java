package com.android.king.xmppdemo.ui;

import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月10日 15:49分
 * @since 2018-09-10
 * @author king
 */
public class SoftInputUtil {

    private static boolean isFirst = true;

    public interface OnGetSoftHeightListener {
        void onShowed(int height);
    }

    public interface OnSoftKeyWordShowListener {
        void hasShow(boolean isShow);
    }

    /**
     * 获取软键盘的高度 * *
     *
     * @param rootView *
     * @param listener
     */
    public static void getSoftKeyboardHeight(final View rootView, final OnGetSoftHeightListener listener) {
        final ViewTreeObserver.OnGlobalLayoutListener layoutListener
                = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isFirst) {
                    final Rect rect = new Rect();
                    rootView.getWindowVisibleDisplayFrame(rect);
                    final int screenHeight = rootView.getRootView().getHeight();
                    final int heightDifference = screenHeight - rect.bottom;
                    //设置一个阀值来判断软键盘是否弹出
                    boolean visible = heightDifference > screenHeight / 3;
                    if (visible) {
                        isFirst = false;
                        if (listener != null) {
                            listener.onShowed(heightDifference);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }


    /**
     * 判断软键盘是否弹出
     * * @param rootView
     *
     * @param listener 备注：在不用的时候记得移除OnGlobalLayoutListener
     */
    public static ViewTreeObserver.OnGlobalLayoutListener doMonitorSoftKeyWord(final View rootView, final OnSoftKeyWordShowListener listener) {
        final ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);
                final int screenHeight = rootView.getRootView().getHeight();
                Log.e("TAG", rect.bottom + "#" + screenHeight);
                final int heightDifference = screenHeight - rect.bottom;
                boolean visible = heightDifference > screenHeight / 3;
                if (listener != null)
                    listener.hasShow(visible);
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        return layoutListener;
    }
}
