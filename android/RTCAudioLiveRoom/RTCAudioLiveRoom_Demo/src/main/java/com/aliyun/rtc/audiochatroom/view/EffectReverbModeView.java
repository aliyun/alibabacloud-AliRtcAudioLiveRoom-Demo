package com.aliyun.rtc.audiochatroom.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.aliyun.rtc.audiochatroom.R;
import com.aliyun.rtc.audiochatroom.rtc.BaseRTCAudioLiveRoom;
import com.aliyun.rtc.audiochatroom.rtc.RtcAudioEffectChangeMode;
import com.aliyun.rtc.audiochatroom.rtc.RtcAudioEffectReverbMode;
import com.aliyun.rtc.audiochatroom.utils.SizeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectReverbModeView extends FrameLayout implements FlowLayout.OnItemSelectedChangeListener, CompoundButton.OnCheckedChangeListener {

    private Switch mSwitchEarBack;
    private TextView mTvSwitchState;
    private FlowLayout mFlowLayoutEffectReverbModes;
    private List<RtcAudioEffectReverbMode> mRtcAudioEffectReverbModes = new ArrayList<>();
    private List<RtcAudioEffectChangeMode> mRtcAudioEffectChangeModes = new ArrayList<>();
    private boolean mEnableEarBack;
    private int mReverbModeSelectPosition, mChangeModeSelectPosition;
    private EffectReverbModeListener mEffectReverbModeListener;
    private FlowLayout mFlowLayoutEffectChangeModes;

    public EffectReverbModeView(@NonNull Context context) {
        this(context, null);
    }

    public EffectReverbModeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EffectReverbModeView(@NonNull Context context, boolean enableEarBack, int reverbModeSelectPosition, int changeModeSelectPosition) {
        this(context);
        mEnableEarBack = enableEarBack;
        mReverbModeSelectPosition = reverbModeSelectPosition;
        mChangeModeSelectPosition = changeModeSelectPosition;
        init();
    }

    public void setEnableEarBack(boolean enableEarBack) {
        mEnableEarBack = enableEarBack;
    }

    public void setReverbModeSelectPosition(int reverbModeSelectPosition) {
        mReverbModeSelectPosition = reverbModeSelectPosition;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.rtc_audioliveroom_layout_audio_effect_reverb_mode, this, true);
        mSwitchEarBack = findViewById(R.id.rtc_audioliveroom_switch_earback);
        mTvSwitchState = findViewById(R.id.rtc_audioliveroom_tv_switch_state);
        mFlowLayoutEffectReverbModes = findViewById(R.id.rtc_audioliveroom_flowlayout_effect_reverb_mode);
        mFlowLayoutEffectChangeModes = findViewById(R.id.rtc_audioliveroom_flowlayout_effect_change_mode);
        mSwitchEarBack.setOnCheckedChangeListener(this);
        initData();
    }

    private void initData() {
        RtcAudioEffectReverbMode[] rtcAudioEffectReverbModes = RtcAudioEffectReverbMode.values();
        mSwitchEarBack.setChecked(mEnableEarBack);
        mRtcAudioEffectReverbModes.clear();
        mRtcAudioEffectReverbModes.addAll(Arrays.asList(rtcAudioEffectReverbModes));
        mFlowLayoutEffectReverbModes.setHorizontalSpacing(SizeUtil.dip2px(getContext(), 8));
        mFlowLayoutEffectReverbModes.setAlignByCenter(FlowLayout.AlienState.LEFT);
        mFlowLayoutEffectReverbModes.setSelectedPosition(mReverbModeSelectPosition);
        mFlowLayoutEffectReverbModes.setOnItemSelectedChangeListener(this);
        mFlowLayoutEffectReverbModes.setAdapter(mRtcAudioEffectReverbModes, R.layout.rtc_audioliveroom_item_effect_reverb_mode, new FlowLayout.BaseItemView<RtcAudioEffectReverbMode>() {
            @Override
            void getCover(RtcAudioEffectReverbMode item, FlowLayout.ViewHolder holder, View inflate, int position) {
                holder.setText(R.id.rtc_audioliveroom_tv_effect_reverb_mode, item.getDes());
            }
        });

        RtcAudioEffectChangeMode[] rtcAudioEffectChangeModes = RtcAudioEffectChangeMode.values();
        mRtcAudioEffectChangeModes.clear();
        mRtcAudioEffectChangeModes.addAll(Arrays.asList(rtcAudioEffectChangeModes));
        mFlowLayoutEffectChangeModes.setHorizontalSpacing(SizeUtil.dip2px(getContext(), 8));
        mFlowLayoutEffectChangeModes.setAlignByCenter(FlowLayout.AlienState.LEFT);
        mFlowLayoutEffectChangeModes.setSelectedPosition(mChangeModeSelectPosition);
        mFlowLayoutEffectChangeModes.setOnItemSelectedChangeListener(this);
        mFlowLayoutEffectChangeModes.setAdapter(mRtcAudioEffectChangeModes, R.layout.rtc_audioliveroom_item_effect_reverb_mode, new FlowLayout.BaseItemView<RtcAudioEffectChangeMode>() {
            @Override
            void getCover(RtcAudioEffectChangeMode item, FlowLayout.ViewHolder holder, View inflate, int position) {
                holder.setText(R.id.rtc_audioliveroom_tv_effect_reverb_mode, item.getDes());
            }
        });
    }

    /**
     * flowlayout selected监听
     * @param position 当前被选中的item
     */
    @Override
    public void onItemSelectedChanged(View view, int position) {
        int id = view.getId();

        if (id == R.id.rtc_audioliveroom_flowlayout_effect_reverb_mode){
            //混音和变声两个功能是互斥的，如果两个方法都调用，那么只有最后调用的方法才生效，不管是不是无效果
            BaseRTCAudioLiveRoom.sharedInstance().setAudioEffectVoiceChangerMode(mRtcAudioEffectChangeModes.get(0).getMode());
            BaseRTCAudioLiveRoom.sharedInstance().setAudioEffectReverbMode(mRtcAudioEffectReverbModes.get(position).getMode());
            mReverbModeSelectPosition = position;
            mFlowLayoutEffectChangeModes.setSelectedPosition(0);
            if (mEffectReverbModeListener != null) {
                mEffectReverbModeListener.onReverbModeSelectedChanged(mReverbModeSelectPosition);
            }
        }else if (id == R.id.rtc_audioliveroom_flowlayout_effect_change_mode){
            BaseRTCAudioLiveRoom.sharedInstance().setAudioEffectReverbMode(mRtcAudioEffectReverbModes.get(0).getMode());
            BaseRTCAudioLiveRoom.sharedInstance().setAudioEffectVoiceChangerMode(mRtcAudioEffectChangeModes.get(position).getMode());
            mChangeModeSelectPosition = position;
            mFlowLayoutEffectReverbModes.setSelectedPosition(0);
            if (mEffectReverbModeListener != null) {
                mEffectReverbModeListener.onChangeModeSelectedChanged(mChangeModeSelectPosition);
            }
        }
    }

    /**
     * switch checjedchanged监听
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mTvSwitchState.setText(isChecked ? R.string.alirtc_audioliveroom_string_switch_open : R.string.alirtc_audioliveroom_string_switch_close);
        mEnableEarBack = isChecked;
        if (mEffectReverbModeListener != null) {
            mEffectReverbModeListener.onCheckedChanged(mEnableEarBack);
        }
        BaseRTCAudioLiveRoom.sharedInstance().enableEarBack(isChecked);
    }

    public void setEffectReverbModeListener(EffectReverbModeListener effectReverbModeListener) {
        mEffectReverbModeListener = effectReverbModeListener;
    }

    public interface EffectReverbModeListener{
        void onReverbModeSelectedChanged(int selectPosition);

        void onChangeModeSelectedChanged(int selectPosition);

        void onCheckedChanged(boolean enableEarBack);
    }
}
