package com.aliyun.rtc.voicecall.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.aliyun.rtc.voicecall.R;
import com.aliyun.rtc.voicecall.utils.BitmapUtil;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

public class BgAdapter extends RecyclerView.Adapter {

    private List<Bitmap> bgBitmaps;
    private Context context;
    private OnBgClickListener listener;
    private int selectedPosition = -1;

    public void setOnBgClickListener(OnBgClickListener listener) {
        this.listener = listener;
    }

    public BgAdapter(List<Bitmap> bgBitmaps, Context context) {
        this.bgBitmaps = bgBitmaps;
        this.context = context;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.alivc_voicecall_rtc_chat_item_bg, parent, false);
        return new BgHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder != null) {
            ((BgHolder) holder).bindView(position);
        }
    }

    @Override
    public int getItemCount() {
        return bgBitmaps == null ? 0 : bgBitmaps.size();
    }

    private class BgHolder extends RecyclerView.ViewHolder {

        private ImageView mIvBg;
        private ImageView mIvCheckbox;
        private TextView mTvBgName;

        public BgHolder(View inflate) {
            super(inflate);
            mIvBg = inflate.findViewById(R.id.alivc_voicecall_item_bg_tv_bg);
            mIvCheckbox = inflate.findViewById(R.id.alivc_voicecall_item_bg_iv_checkbox);
            mTvBgName = inflate.findViewById(R.id.alivc_voicecall_tv_bg_name);
        }

        public void bindView(final int position) {
            mIvCheckbox.setSelected(selectedPosition == position);
            Bitmap roundrectImage = BitmapUtil.createRoundrectImage(bgBitmaps.get(position));
            mIvBg.setImageBitmap(roundrectImage);
            mIvBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedPosition(position);
                    if (listener != null) {
                        listener.onBgClickListener(bgBitmaps.get(position));
                    }
                }
            });
            if (position == 0) {
                mTvBgName.setText(R.string.alivc_voicecall_string_bg_name_1);
            } else if (position == 1) {
                mTvBgName.setText(R.string.alivc_voicecall_string_bg_name_2);
            } else if (position == 2) {
                mTvBgName.setText(R.string.alivc_voicecall_string_bg_name_3);
            }
        }
    }

    public interface OnBgClickListener {
        void onBgClickListener(Bitmap bitmap);
    }
}
