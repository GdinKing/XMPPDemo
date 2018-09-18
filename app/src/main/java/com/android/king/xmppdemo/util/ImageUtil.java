package com.android.king.xmppdemo.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/**
 * @author：King
 * @time: 2018/9/13 20:08
 */
public class ImageUtil {

    public static void showImage(Context context, ImageView imageView, String path) {
        if (context == null) {
            return;
        }
        Glide.with(context).load(path).into(imageView);
    }

    public static void showImage(Context context, ImageView imageView, byte[] bytes) {
        Glide.with(context).load(bytes).into(imageView);
    }

    public static void showImage(Context context, ImageView imageView, byte[] bytes, int defaultImage) {
        RequestOptions opt = new RequestOptions();
        opt.error(defaultImage);
        opt.placeholder(defaultImage);
        Glide.with(context)
                .load(bytes)
                .apply(opt)
                .into(imageView);
    }
}
