package com.android.king.xmppdemo.util;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;

import com.android.king.xmppdemo.R;
import com.lzy.imagepicker.loader.ImageLoader;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月13日 18:09分
 * @since 2018-09-13
 * @author king
 */
public class GlideUtil implements ImageLoader {

    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
        GlideApp.with(activity).load(path).into(imageView);
    }

    @Override
    public void displayImagePreview(Activity activity, String path, ImageView imageView, int width, int height) {

    }

    @Override
    public void clearMemoryCache() {
        //这里是清除缓存的方法,根据需要自己实现
    }
}
