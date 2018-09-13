package com.android.king.xmppdemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.entity.ChatBean;
import com.android.king.xmppdemo.util.CommonUtil;
import com.owater.library.CircleTextView;

import org.jxmpp.jid.DomainBareJid;

import java.util.List;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 10:14分
 * @since 2018-09-04
 * @author king
 */
public class ChatAdapter extends BaseAdapter {

    private List<ChatBean> dataList;
    private Context mContext;


    public ChatAdapter(Context mContext, List<ChatBean> dataList) {
        this.dataList = dataList;
        this.mContext = mContext;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat, null);

            holder = new ViewHolder();
            holder.init(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ChatBean chatBean = dataList.get(position);

        holder.tvName.setText(chatBean.getTitle());
        holder.tvMessage.setText(chatBean.getMessage());
        holder.tvTime.setText(CommonUtil.formatTime(chatBean.getTime()));
        int avatar = chatBean.getAvatar();
        if (avatar != 0) {
            holder.ivAvatar.setImageResource(avatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
        holder.badgeView.setText(chatBean.getUnreadCount() > 99 ? "99+" : String.valueOf(chatBean.getUnreadCount()));
        if (chatBean.getUnreadCount() > 0) {
            holder.badgeView.setVisibility(View.VISIBLE);
        } else {
            holder.badgeView.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void refreshData(List<ChatBean> beanList) {
        this.dataList = beanList;
        notifyDataSetChanged();
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
            ChatBean bean = dataList.get(i);
            if (bean.getTarget().equals(user)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取所有未读数
     *
     * @return
     */
    public int getTotalUnread() {
        int result = 0;
        for (ChatBean bean : dataList) {
            result += bean.getUnreadCount();
        }
        return result;
    }


    class ViewHolder {
        TextView tvName;
        TextView tvMessage;
        TextView tvTime;
        ImageView ivAvatar;
        CircleTextView badgeView;


        public void init(View v) {
            tvName = v.findViewById(R.id.tv_name);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime = v.findViewById(R.id.tv_time);
            ivAvatar = v.findViewById(R.id.iv_avatar);
            badgeView = v.findViewById(R.id.bv_count);
        }
    }
}
