package com.aliyun.rtc.audiochatroom.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliyun.rtc.audiochatroom.R;
import com.aliyun.rtc.audiochatroom.bean.RtcAudioFileInfo;

import java.util.List;

public class RtcBgmAdapter extends RecyclerView.Adapter {
    private List<RtcAudioFileInfo> mFileInfos;
    private Context mContext;
    private AudioPlayingListener mListener;
    private int selectedPosition = -1;

    public RtcBgmAdapter(List<RtcAudioFileInfo> fileInfos, Context context) {
        mFileInfos = fileInfos;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.rtc_audioliveroom_item_bgm, parent, false);
        return new RtcBgmHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((RtcBgmHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mFileInfos == null ? 0 : mFileInfos.size();
    }

    private class RtcBgmHolder extends RecyclerView.ViewHolder {

        private TextView mTvBgmName;
        private SeekBar mSeekBar;
        private ImageView mIvPrePlay;
        private ImageView mIvPlay;

        public RtcBgmHolder(View itemView) {
            super(itemView);
            mTvBgmName = itemView.findViewById(R.id.rtc_audioliveroom_tv_bgm_name);
            mSeekBar = itemView.findViewById(R.id.rtc_audioliveroom_progress);
            mIvPrePlay = itemView.findViewById(R.id.rtc_audioliveroom_iv_preplay);
            mIvPlay = itemView.findViewById(R.id.rtc_audioliveroom_iv_play);
        }

        public void bindView(final int position) {
            final RtcAudioFileInfo rtcAudioFileInfo = mFileInfos.get(position);
            String name = rtcAudioFileInfo.file.getName();
            String[] split = name.split("\\.");
            if (split.length > 0) {
                mTvBgmName.setText(split[0]);
            }
            int playState = rtcAudioFileInfo.playState;
            int prePlayState = rtcAudioFileInfo.prePlayState;
            int playResId = getPlayResId(playState);
            int preplayResId = getPreplayResId(prePlayState);
            mIvPlay.setBackgroundResource(playResId);
            mIvPrePlay.setBackgroundResource(preplayResId);
            mSeekBar.setProgress(rtcAudioFileInfo.volume);
            mIvPrePlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedPosition != -1 && selectedPosition != position){
                        RtcAudioFileInfo selectedAudioFileInfo = mFileInfos.get(selectedPosition);
                        selectedAudioFileInfo.prePlayState = selectedAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
                        notifyItemChanged(selectedPosition);
                    }
                    selectedPosition = position;

                    rtcAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
                    int oldPlayState = rtcAudioFileInfo.prePlayState;
                    if (oldPlayState == RtcAudioFileInfo.PLAYING) {
                        rtcAudioFileInfo.prePlayState = RtcAudioFileInfo.STOP;
                    }else {
                        rtcAudioFileInfo.prePlayState = RtcAudioFileInfo.PLAYING;
                    }
                    if (mListener != null) {
                        mListener.onPlayStateChange(rtcAudioFileInfo);
                    }
                    notifyItemChanged(position);
                }
            });

            mIvPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (selectedPosition != -1 && selectedPosition != position){
                        RtcAudioFileInfo selectedAudioFileInfo = mFileInfos.get(selectedPosition);
                        selectedAudioFileInfo.prePlayState = selectedAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
                        notifyItemChanged(selectedPosition);
                    }
                    selectedPosition = position;

                    rtcAudioFileInfo.prePlayState = RtcAudioFileInfo.PERPARE;
                    int oldPlayState = rtcAudioFileInfo.playState;
                    if (oldPlayState == RtcAudioFileInfo.PLAYING) {
                        rtcAudioFileInfo.playState = RtcAudioFileInfo.STOP;
                    } else {
                        rtcAudioFileInfo.playState = RtcAudioFileInfo.PLAYING;
                    }
                    if (mListener != null) {
                        mListener.onPlayStateChange(rtcAudioFileInfo);
                    }
                    notifyItemChanged(position);
                }
            });

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    rtcAudioFileInfo.volume = progress;
                    if (mListener != null) {
                        mListener.onVolumeChange(rtcAudioFileInfo);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

        }
    }

    private int getPreplayResId(int prePlayState) {
        return prePlayState == RtcAudioFileInfo.PLAYING || prePlayState == RtcAudioFileInfo.RESUME ? R.drawable.rtc_audioliveroom_pause_icon : R.drawable.rtc_audioliveroom_preplay_icon;
    }

    private int getPlayResId(int playState) {
        return playState == RtcAudioFileInfo.PLAYING || playState == RtcAudioFileInfo.RESUME ? R.drawable.rtc_audioliveroom_pause_icon : R.drawable.rtc_audioliveroom_play_icon;
    }

    public void setListener(AudioPlayingListener listener) {
        mListener = listener;
    }

    public interface AudioPlayingListener {
        /**
         * 试听/播放状态改变
         */
        void onPlayStateChange(RtcAudioFileInfo rtcAudioFileInfo);


        void onVolumeChange(RtcAudioFileInfo rtcAudioFileInfo);
    }
}
