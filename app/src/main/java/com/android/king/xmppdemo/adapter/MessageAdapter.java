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
        InTextHolder inTextHolder = null;
        OutTextHolder outTextHolder = null;
        InImageHolder inImageHolder = null;
        OutImageHolder outImageHolder = null;

        int type = getItemViewType(position);
        if (convertView == null) {

            switch (type) {
                case TYPE_TEXT_IN:
                    convertView = mInflater.inflate(R.layout.item_msg_in_text, null);
                    inTextHolder = new InTextHolder();
                    inTextHolder.init(convertView);
                    convertView.setTag(inTextHolder);
                    break;
                case TYPE_TEXT_OUT:
                    convertView = mInflater.inflate(R.layout.item_msg_out_text, null);
                    outTextHolder = new OutTextHolder();
                    outTextHolder.init(convertView);
                    convertView.setTag(outTextHolder);
                    break;
                case TYPE_IMAGE_IN:
                    convertView = mInflater.inflate(R.layout.item_msg_in_image, null);
                    inImageHolder = new InImageHolder();
                    inImageHolder.init(convertView);
                    convertView.setTag(inImageHolder);
                    break;
                case TYPE_IMAGE_OUT:
                    convertView = mInflater.inflate(R.layout.item_msg_out_image, null);
                    outImageHolder = new OutImageHolder();
                    outImageHolder.init(convertView);
                    convertView.setTag(outImageHolder);
                    break;
            }
        } else {
            switch (type) {
                case TYPE_TEXT_IN:
                    inTextHolder = (InTextHolder) convertView.getTag();
                    break;
                case TYPE_TEXT_OUT:
                    outTextHolder = (OutTextHolder) convertView.getTag();
                    break;
                case TYPE_IMAGE_IN:
                    inImageHolder = (InImageHolder) convertView.getTag();
                    break;
                case TYPE_IMAGE_OUT:
                    outImageHolder = (OutImageHolder) convertView.getTag();
                    break;

            }
        }
        MessageBean bean = dataList.get(position);
        setInText(inTextHolder, bean);
        setOutText(outTextHolder, bean, position);
        setIntImage(inImageHolder, bean);
        setOutImage(outImageHolder, bean);
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

    private void setInText(InTextHolder inTextHolder, MessageBean bean) {
        if (inTextHolder == null) {
            return;
        }
        if (Math.abs(bean.getTime() - initTime) > 2 * 60 * 1000) {
            inTextHolder.tvTime.setVisibility(View.VISIBLE);
        } else {
            inTextHolder.tvTime.setVisibility(View.GONE);
        }
        inTextHolder.tvName.setVisibility(bean.getType() == AppConstants.ChatType.SINGLE ? View.GONE : View.VISIBLE);
        inTextHolder.tvName.setText(bean.getFrom());
        inTextHolder.tvMessage.setText(bean.getContent());
        inTextHolder.tvTime.setText(CommonUtil.formatMsgTime(bean.getTime()));
        inTextHolder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        initTime = bean.getTime();
    }

    private void setOutText(OutTextHolder outTextHolder, MessageBean bean, final int position) {
        if (outTextHolder == null) {
            return;
        }
        if (Math.abs(bean.getTime() - initTime) > 2 * 60 * 1000) {
            outTextHolder.tvTime.setVisibility(View.VISIBLE);
        } else {
            outTextHolder.tvTime.setVisibility(View.GONE);
        }
        if (bean.getStatus() == AppConstants.MessageStatus.ERROR) {
            outTextHolder.ivError.setVisibility(View.VISIBLE);
        } else {
            outTextHolder.ivError.setVisibility(View.GONE);
        }
        outTextHolder.tvName.setVisibility(bean.getType() == AppConstants.ChatType.SINGLE ? View.GONE : View.VISIBLE);
        outTextHolder.tvName.setText(bean.getFrom());
        outTextHolder.tvMessage.setText(bean.getContent());
        outTextHolder.tvTime.setText(CommonUtil.formatMsgTime(bean.getTime()));
        outTextHolder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);

        outTextHolder.ivError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onResend(position);
                }
            }
        });
        initTime = bean.getTime();
    }

    private void setIntImage(InImageHolder inImageHolder, MessageBean bean) {
        if (inImageHolder == null) {
            return;
        }
        if (Math.abs(bean.getTime() - initTime) > 2 * 60 * 1000) {
            inImageHolder.tvTime.setVisibility(View.VISIBLE);
        } else {
            inImageHolder.tvTime.setVisibility(View.GONE);
        }
        inImageHolder.tvName.setVisibility(bean.getType() == AppConstants.ChatType.SINGLE ? View.GONE : View.VISIBLE);
        inImageHolder.tvName.setText(bean.getFrom());
        inImageHolder.ivImage.setImageResource(R.drawable.ic_default_avatar);
        inImageHolder.tvTime.setText(CommonUtil.formatMsgTime(bean.getTime()));
        inImageHolder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);

    }

    private void setOutImage(OutImageHolder outImageHolder, MessageBean bean) {
        if (outImageHolder == null) {
            return;
        }
        if (Math.abs(bean.getTime() - initTime) > 2 * 60 * 1000) {
            outImageHolder.tvTime.setVisibility(View.VISIBLE);
        } else {
            outImageHolder.tvTime.setVisibility(View.GONE);
        }
        outImageHolder.tvName.setVisibility(bean.getType() == AppConstants.ChatType.SINGLE ? View.GONE : View.VISIBLE);
        outImageHolder.tvName.setText(bean.getFrom());
        outImageHolder.ivImage.setImageResource(R.drawable.ic_default_avatar);
        outImageHolder.tvTime.setText(CommonUtil.formatMsgTime(bean.getTime()));
        outImageHolder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);

    }


    static class InTextHolder {
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

    static class OutTextHolder {
        TextView tvName;
        EmojiconTextView tvMessage;
        TextView tvTime;
        ImageView ivAvatar;
        ImageView ivError;


        public void init(View v) {
            tvName = v.findViewById(R.id.tv_name);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime = v.findViewById(R.id.tv_time);
            ivAvatar = v.findViewById(R.id.iv_avatar);
            ivError = v.findViewById(R.id.iv_error);
        }
    }

    static class InImageHolder {
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

    static class OutImageHolder {
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
