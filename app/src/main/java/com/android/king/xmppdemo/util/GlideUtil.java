package com.android.king.xmppdemo.util;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import com.android.king.albumpicker.util.ImageLoader;
import com.android.king.xmppdemo.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月13日 18:09分
 * @since 2018-09-13
 * @author king
 */
public class GlideUtil implements ImageLoader {

    @Override
    public void showImage(Context context, String path, ImageView imageView) {

        RequestOptions options = new RequestOptions();
        options.placeholder(R.drawable.album_ic_placeholder);
        options.fitCenter();
        Glide.with(context).load(path) //Glide
                .apply(options)
                .into(imageView);

    }
}
