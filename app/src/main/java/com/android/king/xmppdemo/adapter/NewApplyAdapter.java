package com.android.king.xmppdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.king.xmppdemo.R;
import com.android.king.xmppdemo.entity.Apply;
import com.android.king.xmppdemo.util.ImageUtil;

import java.util.List;

/***
 * 名称：
 * 描述：
 * 最近修改时间：2018年09月04日 10:14分
 * @since 2018-09-04
 * @author king
 */
public class NewApplyAdapter extends BaseAdapter {

    private List<Apply> dataList;
    private Context mContext;

    private OnAgreeClickListener agreeClickListener;

    public NewApplyAdapter(Context mContext, List<Apply> dataList) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_new_apply, null);
            holder = new ViewHolder();
            holder.init(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Apply apply = dataList.get(position);
        holder.tvName.setText(apply.getUser().getName());
        if(apply.getStatus()==Apply.STATUS_IGNORE){
            holder.tvIgnore.setText("已忽略");
            holder.tvIgnore.setEnabled(false);
            holder.tvAgree.setVisibility(View.GONE);
        }else {
            holder.tvIgnore.setText("忽略");
            holder.tvIgnore.setEnabled(true);
            holder.tvAgree.setVisibility(View.VISIBLE);
            holder.tvAgree.setEnabled(apply.getStatus() == Apply.STATUS_UNAGREE);
        }
        holder.tvAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agreeClickListener != null) {
                    agreeClickListener.onAgree(position);
                }
            }
        });
        holder.tvIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agreeClickListener != null) {
                    agreeClickListener.onIgnore(position);
                }
            }
        });
        if (apply.getUser().getAvatar() != null) {
            ImageUtil.showImage(mContext, holder.ivAvatar, apply.getUser().getAvatar());
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
        return convertView;
    }

    public void refreshData(List<Apply> beanList) {
        this.dataList = beanList;
        notifyDataSetChanged();
    }


    public interface OnAgreeClickListener {
        void onAgree(int position);
        void onIgnore(int position);
    }

    public void setAgreeClickListener(OnAgreeClickListener agreeClickListener) {
        this.agreeClickListener = agreeClickListener;
    }

    static class ViewHolder {
        TextView tvName;
        TextView tvAgree;
        TextView tvIgnore;
        ImageView ivAvatar;


        public void init(View v) {
            tvName = v.findViewById(R.id.tv_name);
            tvAgree = v.findViewById(R.id.tv_agree);
            tvIgnore = v.findViewById(R.id.tv_ignore);
            ivAvatar = v.findViewById(R.id.iv_avatar);
        }
    }
}
