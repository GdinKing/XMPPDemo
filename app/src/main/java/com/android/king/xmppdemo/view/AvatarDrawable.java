package com.android.king.xmppdemo.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/***
 * 自定义头像背景图
 *
 * @since 2018/09/14
 * @author king
 */

public class AvatarDrawable extends Drawable {

    private Paint mPaint;
    private Paint mIconPaint;

    private Bitmap mBitmap;
    private Canvas mCanvas;

    private Path linePath;

    private int mWidth;
    private int mHeight;
    private int backgroundColor;
    private int borderWidth = 0;
    private int borderColor = Color.TRANSPARENT;
    private String textIcon;

    public AvatarDrawable(int width, int height, int backgroundColor) {
        this.mWidth = width;
        this.mHeight = height;
        this.backgroundColor = backgroundColor;
        init();
    }

    public void init() {
        //边框画笔
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(borderWidth);
        mPaint.setColor(borderColor);


        mIconPaint = new Paint();
        mIconPaint.setStyle(Paint.Style.STROKE);
        mIconPaint.setAntiAlias(true);
        mIconPaint.setStrokeWidth(5.0f);
        mIconPaint.setTextSize(35.0f);
        mIconPaint.setColor(Color.WHITE);


        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(backgroundColor);
        doDraw();
    }

    private void doDraw() {
        Rect rect = new Rect(0, 0, mWidth, mHeight);
        mCanvas.drawRect(rect, mPaint);
        if (!TextUtils.isEmpty(textIcon)) {
            // 绘制印章名字
            mCanvas.drawText(textIcon, rect.centerX(), rect.bottom - 2 * borderWidth, mIconPaint);
        }
    }

    public void setTextIcon(String textIcon) {
        this.textIcon = textIcon;
        doDraw();
        invalidateSelf();
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        mPaint.setStrokeWidth(borderWidth);
        doDraw();
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    @Override
    public void setAlpha(int i) {
        mPaint.setAlpha(i);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
