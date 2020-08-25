package com.aliyun.rtc.audiochatroom.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.rtc.audiochatroom.R;
import com.aliyun.rtc.audiochatroom.bean.SeatInfo;
import com.aliyun.rtc.audiochatroom.view.WaterImageView;

import java.util.List;

public class SeatListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<SeatInfo> mUsers;

    public SeatListAdapter(Context context, List<SeatInfo> users) {
        mContext = context;
        mUsers = users;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.rtc_audioliveroom_item_user_list, parent, false);
        return new UserInfoHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UserInfoHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mUsers != null ? mUsers.size() : 0;
    }

    private class UserInfoHolder extends RecyclerView.ViewHolder {

        private final WaterImageView mFunctionIcon;
        private final TextView mFunctionName;
        private final ImageView mIvMuteMic;

        public UserInfoHolder(View inflate) {
            super(inflate);
            mFunctionIcon = inflate.findViewById(R.id.alivc_big_interactive_class_iv_bottom_function_icon);
            mIvMuteMic = inflate.findViewById(R.id.rtc_audioliveroom_iv_mute_mic);
            mFunctionName = inflate.findViewById(R.id.alivc_big_interactive_class_tv_bottom_function_name);
        }

        public synchronized void bindView(final int position) {
            SeatInfo seatInfo = mUsers.get(position);
            if (seatInfo == null) {
                mFunctionIcon.setBackgroundResource(R.drawable.rtc_audioliveroom_empty_use_display);
                mFunctionName.setText(String.format(mContext.getResources().getString(R.string.alirtc_audioliveroom_string_username_normal), position + 1));
                mFunctionIcon.stopWaterAnimation();
                mIvMuteMic.setVisibility(View.INVISIBLE);
                return;
            }
            mIvMuteMic.setVisibility(seatInfo.isMuteMic() ? View.VISIBLE : View.INVISIBLE);
            mFunctionName.setText(seatInfo.getUserName());
            int drawable = mContext.getResources().getIdentifier("rtc_audioliveroom_use_display_" + (position + 1), "drawable", mContext.getPackageName());
            mFunctionIcon.setBackgroundResource(drawable);
            if (seatInfo.isSpeaking() && !seatInfo.isMuteMic()) {
                mFunctionIcon.startWaterAnimation();
            } else if (!seatInfo.isSpeaking() || seatInfo.isMuteMic()) {
                mFunctionIcon.stopWaterAnimation();
            }
        }
    }

}
