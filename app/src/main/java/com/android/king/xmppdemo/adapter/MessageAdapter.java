package com.android.king.xmppdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.config.AppConstants;
import com.android.king.xmppdemo.entity.MessageBean;
import com.android.king.xmppdemo.util.CommonUtil;

import java.util.List;

import io.github.rockerhieu.emojicon.EmojiconTextView;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 10:14分
 * @since 2018-09-04
 * @author king
 */
public class MessageAdapter extends BaseAdapter {

    private static final int TYPE_TEXT_IN = 0;
    private static final int TYPE_TEXT_OUT = 1;
    private static final int TYPE_IMAGE_IN = 2;
    private static final int TYPE_IMAGE_OUT = 3;

    private List<MessageBean> dataList;
    private Context mContext;
    private LayoutInflater mInflater;
    private long initTime;
    private OnResendLitener listener;


    public MessageAdapter(Context mContext, List<MessageBean> dataList) {
        this.dataList = dataList;
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
        initTime = System.currentTimeMillis();
    }

    @Override
    public int getCount() {
        if (dataList == null) {
            return 0;
        }
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        if (dataList == null) {
            return null;
        }
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        switch (dataList.get(position).getCategory()) {
            case AppConstants.MessageType.IN_TEXT:
                return TYPE_TEXT_IN;
            case AppConstants.MessageType.OUT_TEXT:
                return TYPE_TEXT_OUT;
            case AppConstants.MessageType.IN_IMAGE:
                return TYPE_IMAGE_IN;
            case AppConstants.MessageType.OUT_IMAGE:
                return TYPE_IMAGE_OUT;
            default:
                return TYPE_TEXT_IN;
        }

    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextHolder textHolder = null;
        ImageHolder imageHolder = null;

        int type = getItemViewType(position);
        if (convertView == null) {

            switch (type) {
                case TYPE_TEXT_IN:
                    convertView = mInflater.inflate(R.layout.item_msg_in_text, null);
                    textHolder = new TextHolder();
                    textHolder.init(convertView);
                    convertView.setTag(textHolder);
                    break;
                case TYPE_TEXT_OUT:
                    convertView = mInflater.inflate(R.layout.item_msg_out_text, null);
                    textHolder = new TextHolder();
                    textHolder.init(convertView);
                    convertView.setTag(textHolder);
                    break;
                case TYPE_IMAGE_IN:
                    convertView = mInflater.inflate(R.layout.item_msg_in_image, null);
                    imageHolder = new ImageHolder();
                    imageHolder.init(convertView);
                    convertView.setTag(imageHolder);
                    break;
                case TYPE_IMAGE_OUT:
                    convertView = mInflater.inflate(R.layout.item_msg_out_image, null);
                    imageHolder = new ImageHolder();
                    imageHolder.init(convertView);
                    convertView.setTag(imageHolder);
                    break;
            }
        } else {
            switch (type) {
                case TYPE_TEXT_IN:
                case TYPE_TEXT_OUT:
                    textHolder = (TextHolder) convertView.getTag();
                    break;
                case TYPE_IMAGE_IN:
                case TYPE_IMAGE_OUT:
                    imageHolder = (ImageHolder) convertView.getTag();
                    break;
            }
        }
        MessageBean bean = dataList.get(position);
        setTextHolder(textHolder, bean);
        setImageHolder(imageHolder, bean);
        return convertView;
    }

    public void refreshData(List<MessageBean> beanList) {
        this.dataList = beanList;
        notifyDataSetChanged();
        initTime = System.currentTimeMillis();
    }

    /**
     * 是否存在与当前用户的对话
     *
     * @param user
     * @return
     */
    public int isExist(String user) {
        if (dataList == null) {
            return -1;
        }
        for (int i = 0; i < dataList.size(); i++) {
            MessageBean bean = dataList.get(i);
            if (bean.getFrom().equals(user)) {
                return i;
            }
        }
        return -1;
    }

    private void setTextHolder(TextHolder textHolder, MessageBean bean) {
        if (textHolder == null) {
            return;
        }
        if (Math.abs(bean.getTime() - initTime) > 3 * 60 * 1000) {
            textHolder.tvTime.setVisibility(View.VISIBLE);
        } else {
            textHolder.tvTime.setVisibility(View.GONE);
        }
        textHolder.tvName.setVisibility(bean.getType() == AppConstants.ChatType.SINGLE ? View.GONE : View.VISIBLE);
        textHolder.tvName.setText(bean.getFrom());
        textHolder.tvMessage.setText(bean.getContent());
        textHolder.tvTime.setText(CommonUtil.formatMsgTime(bean.getTime()));
        textHolder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        initTime = bean.getTime();
    }



    private void setImageHolder(ImageHolder imageHolder, MessageBean bean) {
        if (imageHolder == null) {
            return;
        }
        if (Math.abs(bean.getTime() - initTime) > 3 * 60 * 1000) {
            imageHolder.tvTime.setVisibility(View.VISIBLE);
        } else {
            imageHolder.tvTime.setVisibility(View.GONE);
        }
        imageHolder.tvName.setVisibility(bean.getType() == AppConstants.ChatType.SINGLE ? View.GONE : View.VISIBLE);
        imageHolder.tvName.setText(bean.getFrom());
        imageHolder.ivImage.setImageResource(R.drawable.ic_default_avatar);
        imageHolder.tvTime.setText(CommonUtil.formatMsgTime(bean.getTime()));
        imageHolder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);

    }


    static class TextHolder {
        TextView tvName;
        EmojiconTextView tvMessage;
        TextView tvTime;
        ImageView ivAvatar;

        public void init(View v) {
            tvName = v.findViewById(R.id.tv_name);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime = v.findViewById(R.id.tv_time);
            ivAvatar = v.findViewById(R.id.iv_avatar);
        }
    }


    static class ImageHolder {
        TextView tvName;
        ImageView ivImage;
        TextView tvTime;
        ImageView ivAvatar;


        public void init(View v) {
            tvName = v.findViewById(R.id.tv_name);
            ivImage = v.findViewById(R.id.iv_image);
            tvTime = v.findViewById(R.id.tv_time);
            ivAvatar = v.findViewById(R.id.iv_avatar);
        }
    }

    public void setOnResendListener(OnResendLitener listener) {
        this.listener = listener;
    }

    interface OnResendLitener {
        void onResend(int position);
    }
}
