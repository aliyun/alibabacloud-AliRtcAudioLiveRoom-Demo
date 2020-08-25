package com.aliyun.rtc.audiochatroom.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.aliyun.rtc.audiochatroom.R;

import java.util.ArrayList;
import java.util.List;

public class BottomFunctionAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<Pair<String, Integer>> mFunctions;
    private FunctionCheckedListener mListener;
    private List<Integer> mSelectedItems;

    public BottomFunctionAdapter(Context context, ArrayList<Pair<String, Integer>> functions) {
        mContext = context;
        mFunctions = functions;
        mSelectedItems = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.rtc_audioliveroom_item_bottom_functions, parent, false);
        return new BottomFunctionHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BottomFunctionHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mFunctions != null ? mFunctions.size() : 0;
    }

    public void setListener(FunctionCheckedListener listener) {
        mListener = listener;
    }

    /**
     * 初始化所有按钮状态
     */
    public void resetButtonState(){
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public void setSelectedItems(int position){
        if (!mSelectedItems.contains(position)){
            mSelectedItems.add(position);
            notifyItemChanged(position);
        }
    }

    private class BottomFunctionHolder extends RecyclerView.ViewHolder {

        private final ImageView mFunctionIcon;
        private final TextView mFunctionName;

        public BottomFunctionHolder(View inflate) {
            super(inflate);
            mFunctionIcon = inflate.findViewById(R.id.alivc_big_interactive_class_iv_bottom_function_icon);
            mFunctionName = inflate.findViewById(R.id.alivc_big_interactive_class_tv_bottom_function_name);
        }

        public void bindView(final int position) {
            int bgResId = mFunctions.get(position).second;
            String str = mFunctions.get(position).first;

            mFunctionIcon.setImageResource(bgResId);
            mFunctionIcon.setBackgroundResource(R.drawable.rtc_audioliveroom_bg_bottom_btn_selector);
            if (mSelectedItems.contains(position)) {
                mFunctionIcon.setSelected(true);
            }else{
                mFunctionIcon.setSelected(false);
            }

            if (position == 0){
                str = mContext.getString(mFunctionIcon.isSelected() ? R.string.alirtc_audioliveroom_string_unmute_mic : R.string.alirtc_audioliveroom_string_mute_mic);
            }else if (position == 1){
                str = mContext.getString(mFunctionIcon.isSelected() ? R.string.alirtc_audioliveroom_string_enable_all_remote_audio : R.string.alirtc_audioliveroom_string_unenable_all_remote_audio);
            }
            mFunctionName.setText(str);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onFunctionChecked(position);
                    }

                    if(mSelectedItems.contains(position)){
                        mSelectedItems.remove(Integer.valueOf(position));
                    }else if (position == 0 || position == 1){
                        //只支持静音和扬声器按钮
                        mSelectedItems.add(position);
                    }
                    notifyItemChanged(position);
                }
            });
        }
    }

    public interface FunctionCheckedListener {
        void onFunctionChecked(int position);
    }
}
