package com.android.king.xmppdemo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.king.xmppdemo.R;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;


/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月07日 10:03分
 * @since 2018-09-07
 * @author king
 */
public class BorderTextView extends AppCompatTextView {


    private int borderColor;
    private int solidColor;
    private float borderRadius = 10.0f;
    private float borderWidth = 1.0f;


    private GradientDrawable drawable;

    public BorderTextView(Context context) {
        this(context, null);
    }

    public BorderTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BorderTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BorderTextView);
        borderWidth = ta.getDimension(R.styleable.BorderTextView_borderWidth, 1.0f);
        borderColor = ta.getColor(R.styleable.BorderTextView_borderColor, getResources().getColor(R.color.blue));
        solidColor = ta.getColor(R.styleable.BorderTextView_solidColor, Color.TRANSPARENT);
        borderRadius = ta.getDimension(R.styleable.BorderTextView_textBorderRadius, getResources().getDimension(R.dimen.space_15));
        ta.recycle();

        init();
    }


    private void init() {
        drawable = new GradientDrawable();
        drawable.setStroke((int) borderWidth, borderColor);
        drawable.setColor(solidColor);
        drawable.setCornerRadius(borderRadius);
        setBackgroundDrawable(drawable);
    }

    /**
     * 设置填充颜色
     *
     * @param color 颜色
     */
    public void setSolidColor(int color) {
        drawable.setColor(color);
        setBackgroundDrawable(drawable);
    }

    /**
     * 设置圆角弧度
     *
     * @param leftTopRadius     左上角弧度
     * @param leftBottomRadius  左下角弧度
     * @param rightTopRadius    右上角弧度
     * @param rightBottomRadius 右下角弧度
     */
    public void setRadius(float leftTopRadius, float leftBottomRadius, float rightTopRadius, float rightBottomRadius) {
        float[] array = new float[]{leftTopRadius, leftTopRadius, rightTopRadius, rightTopRadius, rightBottomRadius, rightBottomRadius, leftBottomRadius, leftBottomRadius};
        drawable.setCornerRadii(array);
        setBackgroundDrawable(drawable);
    }

    /**
     * 设置边框颜色
     *
     * @param color 边框颜色
     */
    public void setBorderColor(int color) {
        this.borderColor = color;
        drawable.setStroke((int) borderWidth, color);
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        drawable.setStroke((int) borderWidth, borderColor);
    }

    public void setFullColor(int color) {
        setBorderColor(color);
        setSolidColor(color);
        setTextColor((color != 0 && color != Color.WHITE) ? Color.WHITE : Color.BLUE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            drawable.setAlpha(120);
            setBackgroundDrawable(drawable);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            drawable.setAlpha(255);
            setBackgroundDrawable(drawable);
        }
        return super.onTouchEvent(event);
    }
}
