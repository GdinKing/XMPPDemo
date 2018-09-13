package com.android.king.xmppdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.entity.User;
import com.android.king.xmppdemo.util.ImageUtil;

import java.util.List;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 10:14分
 * @since 2018-09-04
 * @author king
 */
public class UserAdapter extends BaseAdapter {

    private List<User> dataList;
    private Context mContext;


    public UserAdapter(Context mContext, List<User> dataList) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_search_user, null);
            holder = new ViewHolder();
            holder.init(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = dataList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvAccount.setText("账号：" + user.getAccount());
        if (user.getAvatar() != null) {
            ImageUtil.showImage(mContext, holder.ivAvatar, user.getAvatar());
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
        return convertView;
    }

    public void refreshData(List<User> beanList) {
        this.dataList = beanList;
        notifyDataSetChanged();
    }


    static class ViewHolder {
        TextView tvName;
        TextView tvAccount;
        ImageView ivAvatar;


        public void init(View v) {
            tvName = v.findViewById(R.id.tv_name);
            tvAccount = v.findViewById(R.id.tv_account);
            ivAvatar = v.findViewById(R.id.iv_avatar);
        }
    }
}
