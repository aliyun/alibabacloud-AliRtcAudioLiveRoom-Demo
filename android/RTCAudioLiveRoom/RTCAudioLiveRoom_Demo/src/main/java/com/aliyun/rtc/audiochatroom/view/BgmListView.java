package com.aliyun.rtc.audiochatroom.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.aliyun.rtc.audiochatroom.R;
import com.aliyun.rtc.audiochatroom.adapter.RtcBgmAdapter;
import com.aliyun.rtc.audiochatroom.bean.RtcAudioFileInfo;
import com.aliyun.rtc.audiochatroom.utils.UIHandlerUtil;

import java.util.ArrayList;
import java.util.List;

public class BgmListView extends FrameLayout {

    private RecyclerView mRcyBgm;
    private List<RtcAudioFileInfo> fileInfos = new ArrayList<>();
    private RtcBgmAdapter.AudioPlayingListener mListener;
    private RtcBgmAdapter mRtcBgmAdapter;

    public BgmListView(Context context){
        super(context);
    }

    public BgmListView(List<RtcAudioFileInfo> fileInfos, Context context){
        this(context);
        this.fileInfos.clear();
        this.fileInfos.addAll(fileInfos);
        initView();
    }

    public void setListener(RtcBgmAdapter.AudioPlayingListener listener) {
        mListener = listener;
        if (mRtcBgmAdapter != null) {
            mRtcBgmAdapter.setListener(listener);
        }
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.rtc_audioliveroom_layout_bgm,this,true);
        mRcyBgm = findViewById(R.id.rtc_audioliveroom_rcy_bgm);
        mRcyBgm.setLayoutManager(new LinearLayoutManager(getContext()));
        mRtcBgmAdapter = new RtcBgmAdapter(fileInfos, getContext());
        mRtcBgmAdapter.setListener(mListener);
        mRcyBgm.setAdapter(mRtcBgmAdapter);
    }

    public void notifyItemChanged(RtcAudioFileInfo rtcAudioFileInfo){
        if (rtcAudioFileInfo == null || fileInfos == null){
            return;
        }
        final int index = fileInfos.indexOf(rtcAudioFileInfo);
        if (index != -1){
            fileInfos.get(index).playState = rtcAudioFileInfo.playState;
            fileInfos.get(index).prePlayState = rtcAudioFileInfo.prePlayState;
            UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                @Override
                public void run() {
                    mRtcBgmAdapter.notifyItemChanged(index);
                }
            });
        }
    }
}
