package com.android.king.xmppdemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
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

    private String keyword;

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

        setText(holder.tvName, user.getName());
        setText(holder.tvAccount, "账号：" + user.getAccount());

//        holder.tvAccount.setText("账号：" + user.getAccount());
        if (user.getAvatar() != null) {
            ImageUtil.showImage(mContext, holder.ivAvatar, user.getAvatar());
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
        return convertView;
    }


    private void setText(TextView textView, String text) {
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf(keyword);
        if (start != -1) {
            int end = start + keyword.length();
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#ff9933")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(ss);
    }

    public void refreshData(List<User> beanList) {
        this.dataList = beanList;
        notifyDataSetChanged();
    }

    public void setKeyWord(String key) {
        this.keyword = key;
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
