package com.aliyun.rtc.voicecall.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.rtc.voicecall.R;
import com.aliyun.rtc.voicecall.utils.DoubleClickUtil;

import java.io.File;
import java.util.List;

public class BgmAdapter extends RecyclerView.Adapter {
    private List<File> files;
    private Context context;
    private OnPlayBtnClickListener listener;
    private int selectedPosition = -1;
    private SparseBooleanArray mBgmStates;

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    public BgmAdapter(List<File> files, Context context) {
        this.files = files;
        this.context = context;
        mBgmStates = new SparseBooleanArray();
    }

    public void setOnPlayBtnClickListener(OnPlayBtnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.alivc_voicecall_rtc_chat_item_bgm, parent, false);
        return new BgmHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder != null) {
            ((BgmHolder) holder).bindView(position);
        }
    }

    @Override
    public int getItemCount() {
        return files == null ? 0 : files.size();
    }

    private class BgmHolder extends RecyclerView.ViewHolder {

        private TextView mTvBgmName;
        private ImageView mIvPlay;
        private LinearLayout mLlContent;

        private BgmHolder(View inflate) {
            super(inflate);
            mTvBgmName = ((TextView) inflate.findViewById(R.id.alivc_voicecall_item_bgm_tv_bgm_name));
            mIvPlay = ((ImageView) inflate.findViewById(R.id.alivc_voicecall_item_bgm_iv_play));
            mLlContent = ((LinearLayout) inflate.findViewById(R.id.alivc_voicecall_bgm_item_ll_content));
        }

        private void bindView(final int position) {
            File file = files.get(position);

            int i = mBgmStates.indexOfKey(position);
            if (i == -1) {
                mBgmStates.append(position, false);
            }

            if (file != null) {
                mTvBgmName.setText(file.getName());
                mIvPlay.setVisibility(View.VISIBLE);
            } else {
                mTvBgmName.setText(R.string.alivc_voicecall_string_no_bgm);
                mIvPlay.setVisibility(View.GONE);
            }


            boolean playing = mBgmStates.get(position);

            if (playing && selectedPosition == position) {
                mIvPlay.setBackgroundResource(R.drawable.alivc_voice_call_bgm_pause);
            } else {
                mIvPlay.setBackgroundResource(R.drawable.alivc_voice_call_bgm_play_icon_selector);
            }

            mLlContent.setSelected(position == selectedPosition);

            mLlContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!DoubleClickUtil.isDoubleClick(v, 200)) {
                        Boolean playing = mBgmStates.get(position);
                        if (position == selectedPosition) {
                            playing = !playing;
                        } else {
                            playing = true;
                        }
                        mBgmStates.put(position, playing);
                        setSelectedPosition(position);
                        if (listener != null) {
                            listener.onPlayBtnClickListener(files.get(position), playing);
                        }
                    }
                }
            });
        }

    }

    public interface OnPlayBtnClickListener {
        void onPlayBtnClickListener(File file, boolean playing);
    }
}
