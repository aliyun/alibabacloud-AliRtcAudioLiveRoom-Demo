package com.aliyun.rtc.audiochatroom.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.device.utils.StringUtils;
import com.aliyun.rtc.alivcrtcviewcommon.listener.OnTipsDialogListener;
import com.aliyun.rtc.alivcrtcviewcommon.widget.RTCDialogHelper;
import com.aliyun.rtc.alivcrtcviewcommon.widget.RTCLoadingDialogHelper;
import com.aliyun.rtc.audiochatroom.R;
import com.aliyun.rtc.audiochatroom.adapter.AutoItemDecoration;
import com.aliyun.rtc.audiochatroom.adapter.BottomFunctionAdapter;
import com.aliyun.rtc.audiochatroom.adapter.RtcBgmAdapter;
import com.aliyun.rtc.audiochatroom.adapter.SeatListAdapter;
import com.aliyun.rtc.audiochatroom.api.BaseRTCAudioLiveRoomApi;
import com.aliyun.rtc.audiochatroom.api.impl.BaseRTCAudioLiveRoomApiImpl;
import com.aliyun.rtc.audiochatroom.api.net.OkhttpClient;
import com.aliyun.rtc.audiochatroom.bean.ChannelUserNumResponse;
import com.aliyun.rtc.audiochatroom.bean.RtcAudioFileInfo;
import com.aliyun.rtc.audiochatroom.bean.SeatInfo;
import com.aliyun.rtc.audiochatroom.constant.Constant;
import com.aliyun.rtc.audiochatroom.rtc.BaseRTCAudioLiveRoom;
import com.aliyun.rtc.audiochatroom.rtc.RTCAudioLiveRoomDelegate;
import com.aliyun.rtc.audiochatroom.rtc.SimpleRTCAudioLiveRoomDelegate;
import com.aliyun.rtc.audiochatroom.runnable.LoadAssetsFileRunnable;
import com.aliyun.rtc.audiochatroom.runnable.RunnableCallBack;
import com.aliyun.rtc.audiochatroom.utils.ClipboardUtil;
import com.aliyun.rtc.audiochatroom.utils.FileUtil;
import com.aliyun.rtc.audiochatroom.utils.NetWatchdogUtils;
import com.aliyun.rtc.audiochatroom.utils.ScreenUtil;
import com.aliyun.rtc.audiochatroom.utils.ThreadUtils;
import com.aliyun.rtc.audiochatroom.utils.ToastUtils;
import com.aliyun.rtc.audiochatroom.utils.UIHandlerUtil;
import com.aliyun.rtc.audiochatroom.view.BgmListView;
import com.aliyun.rtc.audiochatroom.view.EffectReverbModeView;
import com.aliyun.rtc.audiochatroom.view.RTCBottomDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_SDK_INVALID_STATE;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_SESSION_REMOVED;

public class RtcChatActivity extends AppCompatActivity implements View.OnClickListener, BottomFunctionAdapter.FunctionCheckedListener, RtcBgmAdapter.AudioPlayingListener, NetWatchdogUtils.NetChangeListener {
    private TextView mTvConnMic;
    private RecyclerView mRcyFunctionViews;
    private RecyclerView mRcyUserList;
    private BottomFunctionAdapter mAdapter;
    private List<SeatInfo> mSeatInfos = new ArrayList<>();
    private SeatListAdapter mSeatInfosAdapter;
    private boolean mConnMic, muteMic, mEarBack;
    //默认开启扬声器
    private boolean mMuteAllRemoteAudioPlaying;
    private List<RtcAudioFileInfo> mBgmFils = new ArrayList<>(), mAudioEffectFiles = new ArrayList<>();
    private ArrayList<Pair<String, Integer>> mFunctions;
    private RTCDialogHelper mRtcDialogHelper;
    private RtcAudioFileInfo mCurrAudioFileInfo;
    private NetWatchdogUtils mNetWatchdogUtils;
    private int mSelectedEffectReverbMode, mSelectedEffectChangeMode;
    private BgmListView mBgmListView;
    private boolean hasShowBadNetwork;
    private BaseRTCAudioLiveRoomApi mRtcAudioLiveApi;
    private String mLocalUserId, mChannelId;

    public static void start(Context context, String channelId, String uid, boolean connMic) {
        Intent intent = new Intent(context, RtcChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("channelId", channelId);
        bundle.putString("uid", uid);
        bundle.putBoolean("connMic", connMic);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View inflate = LayoutInflater.from(RtcChatActivity.this).inflate(R.layout.alirtc_audio_live_room_activity_chat, null);
        int statusBarHeight = ScreenUtil.getStatusBarHeight(RtcChatActivity.this);
        inflate.setPadding(0, statusBarHeight, 0, 0);
        setContentView(inflate);
        initData();
        initView();
        mRtcAudioLiveApi = new BaseRTCAudioLiveRoomApiImpl();
        BaseRTCAudioLiveRoom.sharedInstance().setRTCAudioLiveRoomDelegate(mCallBack);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mNetWatchdogUtils == null) {
            //添加网络监听
            mNetWatchdogUtils = new NetWatchdogUtils(RtcChatActivity.this);
            mNetWatchdogUtils.setNetChangeListener(this);
            mNetWatchdogUtils.startWatch();
        }
    }

    private void initData() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mChannelId = extras.getString("channelId");
            mLocalUserId = extras.getString("uid");
            mConnMic = extras.getBoolean("connMic", false);
            initBgmAndAudioEffect();
        }
    }

    /**
     * 加载assets中的音频文件
     */
    private void initBgmAndAudioEffect() {
        String bgmOutPath = FileUtil.getExternalCacheDirPath(RtcChatActivity.this, Constant.PATH_DIR_BGM_OUT);
        String audioEffectOutPath = FileUtil.getExternalCacheDirPath(RtcChatActivity.this, Constant.PATH_DIR_AUDIOEFFECT_OUT);
        //bgm
        ThreadUtils.runOnSubThread(new LoadAssetsFileRunnable(RtcChatActivity.this, Constant.PATH_ASSETS_BGM, bgmOutPath, new RunnableCallBack<List<File>>() {
            @Override
            public void callBack(List<File> data) {
                mBgmFils.clear();
                for (File datum : data) {
                    RtcAudioFileInfo info = new RtcAudioFileInfo();
                    info.file = datum;
                    info.volume = Constant.VALUE_AUDIO_EFFECT_VOLUME;
                    mBgmFils.add(info);
                }
            }
        }));
        //audioeffect
        ThreadUtils.runOnSubThread(new LoadAssetsFileRunnable(RtcChatActivity.this, Constant.PATH_ASSETS_AUDIOEFFECT, audioEffectOutPath, new RunnableCallBack<List<File>>() {
            @Override
            public void callBack(List<File> data) {
                mAudioEffectFiles.clear();
                Collections.sort(data, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        boolean b = StringUtils.equals(o1.getName(), "笑声");
                        return !b ? -1 : 0;
                    }
                });
                for (File datum : data) {
                    RtcAudioFileInfo info = new RtcAudioFileInfo();
                    info.file = datum;
                    info.volume = Constant.VALUE_AUDIO_EFFECT_VOLUME;
                    mAudioEffectFiles.add(info);
                }
            }
        }));
    }

    private void initView() {
        TextView tvChannelId = findViewById(R.id.rtc_audioliveroom_tv_channel_id);
        ImageView ivCopy = findViewById(R.id.rtc_audioliveroom_iv_copy);
        ImageView ivExit = findViewById(R.id.rtc_audioliveroom_iv_exit);
        mTvConnMic = findViewById(R.id.rtc_audioliveroom_tv_conn_mic);
        mRcyFunctionViews = findViewById(R.id.rtc_audioliveroom_rcy_function_views);
        mRcyUserList = findViewById(R.id.rtc_audioliveroom_rcy_user_list);
        tvChannelId.setText(String.format(getString(R.string.alirtc_audioliveroom_string_channel_id_tv), mChannelId));

        initFunctionViews();
        initUserList();

        mTvConnMic.setOnClickListener(this);
        ivCopy.setOnClickListener(this);
        ivExit.setOnClickListener(this);
        reflushConnMic();
    }

    private void initFunctionViews() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRcyFunctionViews.setLayoutManager(linearLayoutManager);
        mFunctions = getFunctions();
        mAdapter = new BottomFunctionAdapter(this, mFunctions);
        mAdapter.setListener(this);
        mRcyFunctionViews.addItemDecoration(new AutoItemDecoration(mFunctions.size()));
        mRcyFunctionViews.setAdapter(mAdapter);
    }

    /**
     * 从资源文件中获取底部功能按钮列表
     *
     * @return 图片资源文件和名称集合
     */
    private ArrayList<Pair<String, Integer>> getFunctions() {
        String[] names = getResources().getStringArray(R.array.functions);
        ArrayList<Pair<String, Integer>> bottomBtns = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            bottomBtns.add(new Pair<>(names[i], getBottombtnResId(i)));
        }
        return bottomBtns;
    }

    private void reflushConnMic() {
        mTvConnMic.setText(mConnMic ? R.string.alirtc_audioliveroom_string_unconn_mic : R.string.alirtc_audioliveroom_string_conn_mic);
        mRcyFunctionViews.setVisibility(mConnMic ? View.VISIBLE : View.GONE);
    }

    private int getBottombtnResId(int i) {
        int resId = -1;
        switch (i) {
            case 0:
                resId = R.drawable.rtc_audioliveroom_bg_mic_selector;
                break;
            case 1:
                resId = R.drawable.rtc_audioliveroom_bg_speakerphone_selector;
                break;
            case 2:
                resId = R.drawable.rtc_audioliveroom_bgm;
                break;
            case 3:
                resId = R.drawable.rtc_audioliveroom_audioeffect;
                break;
            case 4:
                resId = R.drawable.rtc_audioliveroom_effect_reverb_mode;
                break;
            default:
        }
        return resId;
    }

    private void initUserList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(RtcChatActivity.this, 4);
        mRcyUserList.setLayoutManager(gridLayoutManager);
        for (int i = 0; i < Constant.MAX_SEAT_COUNT; i++) {
            mSeatInfos.add(null);
        }
        mSeatInfosAdapter = new SeatListAdapter(this, mSeatInfos);
        mRcyUserList.setAdapter(mSeatInfosAdapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        //复制
        if (id == R.id.rtc_audioliveroom_iv_copy) {
            boolean copy = ClipboardUtil.copy(RtcChatActivity.this, mChannelId);
            String msg = getString(copy ? R.string.alirtc_audioliveroom_string_copy_success : R.string.alirtc_audioliverooms_string_copy_faild);
            showToastInCenter(msg);
            //退出房间
        } else if (id == R.id.rtc_audioliveroom_iv_exit) {
            showExitDialog();
            //连麦
        } else if (id == R.id.rtc_audioliveroom_tv_conn_mic) {
            showLoading();
            mConnMic = !mConnMic;
            toggleConnMicState();

        }
    }

    private void showToastInCenter(final String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ToastUtils.showInCenter(RtcChatActivity.this, msg);
        } else {
            UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showInCenter(RtcChatActivity.this, msg);
                }
            });
        }
    }

    private void toggleConnMicState() {
        if (!mConnMic) {
            initRTCStatus();
            BaseRTCAudioLiveRoom.sharedInstance().leaveSeat();
        } else {
            mRtcAudioLiveApi.describeChannelUsers(mChannelId, new OkhttpClient.BaseHttpCallBack<ChannelUserNumResponse>() {
                @Override
                public void onSuccess(ChannelUserNumResponse data) {
                    if (data == null || data.getData() == null) {
                        hideLoading();
                        return;
                    }
                    List<String> interactiveUserList = data.getData().getInteractiveUserList();
                    if (interactiveUserList.size() < Constant.MAX_SEAT_COUNT) {
                        //连麦时判断下已连麦人数
                        BaseRTCAudioLiveRoom.sharedInstance().enterSeat();
                    } else {
                        hideLoading();
                        showToastInCenter(getString(R.string.alirtc_audioliveroom_string_channel_user_num_empty));
                    }
                }

                @Override
                public void onError(String errorMsg) {
                    hideLoading();
                    showToastInCenter(getString(R.string.alirtc_audioliveroom_string_load_channel_user_num_error));
                }
            });
        }
    }

    private void changeUserMicState(String userid, boolean muteMic) {
        SeatInfo seatInfo = new SeatInfo();
        seatInfo.setUserId(userid);
        if (mSeatInfos != null && mSeatInfos.contains(seatInfo)) {
            int index = mSeatInfos.indexOf(seatInfo);
            mSeatInfos.get(index).setMuteMic(muteMic);
            mSeatInfosAdapter.notifyItemChanged(index);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRtcDialogHelper != null) {
            mRtcDialogHelper.release();
        }
        RTCLoadingDialogHelper.getInstance().release();
        ToastUtils.cancel();
        //        UIHandlerUtil.getInstance().clearAllMsgAndRunnable();
        if (mNetWatchdogUtils != null) {
            mNetWatchdogUtils.stopWatch();
            mNetWatchdogUtils.setNetChangeListener(null);
            mNetWatchdogUtils = null;
        }
    }

    private void showBgmDialog(List<RtcAudioFileInfo> datas, String title) {
        RTCBottomDialog mBottomDialog = new RTCBottomDialog(RtcChatActivity.this);
        mBottomDialog.setTitle(title);
        mBgmListView = new BgmListView(datas, RtcChatActivity.this);
        mBgmListView.setListener(this);
        mBottomDialog.setContentView(mBgmListView);
        mBottomDialog.show();
    }

    private void showEffectReverbDialog(String title) {
        RTCBottomDialog bottomDialog = new RTCBottomDialog(RtcChatActivity.this);
        bottomDialog.setTitle(title);
        EffectReverbModeView effectReverbModeView = new EffectReverbModeView(RtcChatActivity.this, mEarBack, mSelectedEffectReverbMode, mSelectedEffectChangeMode);
        effectReverbModeView.setEffectReverbModeListener(new EffectReverbModeView.EffectReverbModeListener() {
            @Override
            public void onReverbModeSelectedChanged(int selectPosition) {
                mSelectedEffectReverbMode = selectPosition;
                mSelectedEffectChangeMode = 0;
            }

            @Override
            public void onChangeModeSelectedChanged(int selectPosition) {
                mSelectedEffectChangeMode = selectPosition;
                mSelectedEffectReverbMode = 0;
            }

            @Override
            public void onCheckedChanged(boolean enableEarBack) {
                mEarBack = enableEarBack;
            }
        });
        bottomDialog.setContentView(effectReverbModeView);
        bottomDialog.show();
    }

    @Override
    public void onFunctionChecked(int position) {
        String title = mFunctions == null ? "" : mFunctions.get(position).first;
        switch (position) {
            //静音
            case 0:
                muteMic = !muteMic;
                int muteMicResult = BaseRTCAudioLiveRoom.sharedInstance().muteLocalMic(muteMic);
                if (muteMicResult == 0) {
                    showToastInCenter(muteMic ? getString(R.string.alirtc_audioliveroom_string_mute_mic_enable) : getString(R.string.alirtc_audioliveroom_string_mute_mic_unenable));
                }
                //刷新用户列表中自己的状态
                changeUserMicState(mLocalUserId, muteMic);
                break;
            //扬声器
            case 1:
                mMuteAllRemoteAudioPlaying = !mMuteAllRemoteAudioPlaying;
                int enableSpeakerPhoneResult = BaseRTCAudioLiveRoom.sharedInstance().muteAllRemoteAudioPlaying(mMuteAllRemoteAudioPlaying);
                if (enableSpeakerPhoneResult == 0) {
                    showToastInCenter(mMuteAllRemoteAudioPlaying ? getString(R.string.alirtc_audioliveroom_string_speaker_phone_enable) : getString(R.string.alirtc_audioliveroom_string_speaker_phone_unenable));
                }
                break;
            //背景乐
            case 2:
                showBgmDialog(mBgmFils, title);
                break;
            //音效
            case 3:
                showBgmDialog(mAudioEffectFiles, title);
                break;
            //调音台
            case 4:
                showEffectReverbDialog(title);
                break;
            default:
        }
    }


    private RTCAudioLiveRoomDelegate mCallBack = new SimpleRTCAudioLiveRoomDelegate() {

        /**
         * 用户上麦通知
         * @param seatInfo 麦位
         */
        @Override
        public void onEnterSeat(SeatInfo seatInfo) {
            super.onEnterSeat(seatInfo);
            if (StringUtils.equals(seatInfo.getUserId(),mLocalUserId)) {
                hideLoading();
            }
            int seatIndex = getSeatIndex(seatInfo.getSeatIndex());
            if (seatIndex >= 0) {
                mSeatInfos.set(seatIndex, seatInfo);
                mSeatInfosAdapter.notifyItemChanged(seatIndex);
            }
        }

        /**
         * 用户下麦通知
         * @param seatInfo 麦位
         */
        @Override
        public void onLeaveSeat(SeatInfo seatInfo) {
            super.onLeaveSeat(seatInfo);
            hideLoading();
            int seatIndex = getSeatIndex(seatInfo.getSeatIndex());
            if (seatIndex >= 0) {
                mSeatInfos.set(seatIndex, null);
                mSeatInfosAdapter.notifyItemChanged(seatIndex);
            }
        }

        /**
         * 退出房间回调
         */
        @Override
        public void onLeaveChannelResult(int result) {
            hideLoading();
            if (result != 0) {
                showToastInCenter(String.valueOf(result));
            }
            if (!mRtcDialogHelper.isShowing()) {
                finish();
            }
        }

        /**
         * 用户音量更新回调
         */
        @Override
        public void onSeatVolumeChanged(int seatIndex, boolean isSpeaking) {
            super.onSeatVolumeChanged(seatIndex, isSpeaking);
            if (seatIndex >= 0) {
                mSeatInfos.get(seatIndex).setSpeaking(isSpeaking);
                mSeatInfosAdapter.notifyItemChanged(seatIndex);
            }
        }

        /**
         * 播放状态更新回调
         * @param audioPlayingStatus 当前播放状态
         */
        @Override
        public void onAudioPlayingStateChanged(AliRtcEngine.AliRtcAudioPlayingStateCode audioPlayingStatus) {
            super.onAudioPlayingStateChanged(audioPlayingStatus);
            if (audioPlayingStatus == AliRtcEngine.AliRtcAudioPlayingStateCode.AliRtcAudioPlayingEnded) {
                //播放完毕
                mCurrAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
                mCurrAudioFileInfo.prePlayState = RtcAudioFileInfo.PERPARE;
                if (mBgmListView != null) {
                    mBgmListView.notifyItemChanged(mCurrAudioFileInfo);
                }
            }
        }

        /**
         * 用户静音回调
         * @param seatIndex 麦序
         * @param mute 是否静音
         */
        @Override
        public void onSeatMutedChanged(int seatIndex, boolean mute) {
            super.onSeatMutedChanged(seatIndex, mute);
            if (seatIndex >= 0) {
                changeUserMicState(mSeatInfos.get(seatIndex).getUserId(), mute);
            }
        }

        /**
         * 体验时长结束
         */
        @Override
        public void onRoomDestroy() {
            showTimeoutDialog();
        }

        /**
         * sdk报错,需要销毁实例
         */
        @Override
        public void onOccurError(int error) {
            //出现这几个错误码需要销毁sdk
            switch (error) {
                case ERR_SDK_INVALID_STATE:
                case ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT:
                case ERR_SESSION_REMOVED:
                    //销毁
                    BaseRTCAudioLiveRoom.sharedInstance().destorySharedInstance();
                    showRtcErrorDialog();
                    break;
                default:
                    break;
            }

        }

        /**
         * 角色切换成功
         * @param oldRole 旧的用户角色
         * @param newRole 新的用户角色
         */
        @Override
        public void onUpdateRoleNotify(AliRtcEngine.AliRTCSDK_Client_Role oldRole, AliRtcEngine.AliRTCSDK_Client_Role newRole) {
            super.onUpdateRoleNotify(oldRole, newRole);
            changeClientRoleSuccess();
        }

        @Override
        public void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1) {
            super.onNetworkQualityChanged(s, aliRtcNetworkQuality, aliRtcNetworkQuality1);
            synchronized (BaseRTCAudioLiveRoom.class) {
                if (aliRtcNetworkQuality1.getValue() >= AliRtcEngine.AliRtcNetworkQuality.Network_Bad.getValue() && aliRtcNetworkQuality1.getValue() <= AliRtcEngine.AliRtcNetworkQuality.Network_VeryBad.getValue() && !hasShowBadNetwork) {//网络质量差
                    hasShowBadNetwork = true;
                    if (TextUtils.isEmpty(s)) {
                        showToastInCenter(getString(R.string.alirtc_audioliveroom_string_network_bad));
                    }
                } else if (TextUtils.isEmpty(s) && aliRtcNetworkQuality1.getValue() <= AliRtcEngine.AliRtcNetworkQuality.Network_Good.getValue()) {
                    hasShowBadNetwork = false;
                }
            }
        }

        @Override
        public void onPublishChangedNotify(int result, boolean isPublished) {
            super.onPublishChangedNotify(result, isPublished);
            if (result != 0) {
                hideLoading();
                showToastInCenter(String.valueOf(result));
            }
        }
    };

    private void changeClientRoleSuccess() {
        //刷新页面按钮状态
        reflushConnMic();
        //刷新底部按钮状态
        mAdapter.resetButtonState();
        //刷新静音小图标的显示
        muteMic = false;
        //开启声音
        mMuteAllRemoteAudioPlaying = false;
        //初始化背景音乐和音效
        mCurrAudioFileInfo = null;
        //初始化音效场景
        mSelectedEffectReverbMode = 0;
        mSelectedEffectChangeMode = 0;
        mEarBack = false;
    }

    /**
     * 初始化背景乐、音效、静音等状态
     */
    private void initRTCStatus() {
        //停止背景音乐和音效
        BaseRTCAudioLiveRoom.sharedInstance().stopAudioAccompany();
        int sourceId = mCurrAudioFileInfo == null ? -1 : mAudioEffectFiles.indexOf(mCurrAudioFileInfo);
        if (sourceId != -1) {
            BaseRTCAudioLiveRoom.sharedInstance().stopAudioEffect(sourceId);
        }
        int index = mBgmFils.indexOf(mCurrAudioFileInfo);
        if (index != -1) {
            mBgmFils.get(index).playState = mBgmFils.get(index).prePlayState = RtcAudioFileInfo.PERPARE;
        }
        //初始化耳返状态
        BaseRTCAudioLiveRoom.sharedInstance().enableEarBack(false);
        //初始化本地静音状态
        BaseRTCAudioLiveRoom.sharedInstance().muteAllRemoteAudioPlaying(false);
        //关闭音效场景
        BaseRTCAudioLiveRoom.sharedInstance().setAudioEffectReverbMode(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Off);
        BaseRTCAudioLiveRoom.sharedInstance().setAudioEffectVoiceChangerMode(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_OFF);
        //初始化静音状态
        BaseRTCAudioLiveRoom.sharedInstance().muteLocalMic(false);
    }

    /**
     * 背景乐、音效播放状态监听
     */
    @Override
    public void onPlayStateChange(RtcAudioFileInfo rtcAudioFileInfo) {
        if (mBgmFils.contains(rtcAudioFileInfo)) {
            flushBgmState(rtcAudioFileInfo);
        } else {
            flushAudioEffectState(rtcAudioFileInfo);
        }
    }

    @Override
    public void onVolumeChange(RtcAudioFileInfo rtcAudioFileInfo) {
        if (mBgmFils.contains(rtcAudioFileInfo)) {
            BaseRTCAudioLiveRoom.sharedInstance().setAudioAccompanyVolume(rtcAudioFileInfo.volume);
        } else {
            BaseRTCAudioLiveRoom.sharedInstance().setAudioEffectVolume(mAudioEffectFiles.indexOf(rtcAudioFileInfo), rtcAudioFileInfo.volume);
        }
    }

    /**
     * 刷新音效的播放状态
     */
    private void flushAudioEffectState(RtcAudioFileInfo rtcAudioFileInfo) {
        if (mCurrAudioFileInfo != rtcAudioFileInfo && mCurrAudioFileInfo != null) {
            mCurrAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
            int index = mAudioEffectFiles.indexOf(mCurrAudioFileInfo);
            if (index != -1) {
                BaseRTCAudioLiveRoom.sharedInstance().stopAudioEffect(index);
            }
        }
        mCurrAudioFileInfo = rtcAudioFileInfo;
        int sourceId = mAudioEffectFiles.indexOf(rtcAudioFileInfo);
        int playState = rtcAudioFileInfo.playState;
        int prePlayState = rtcAudioFileInfo.prePlayState;
        if (prePlayState == RtcAudioFileInfo.PLAYING || prePlayState == RtcAudioFileInfo.STOP) {
            rtcAudioFileInfo.prePlayState = RtcAudioFileInfo.PERPARE;
            BaseRTCAudioLiveRoom.sharedInstance().playAudioEffect(sourceId, rtcAudioFileInfo.file.getAbsolutePath(), 1, false);
        } else if (playState == RtcAudioFileInfo.PLAYING || playState == RtcAudioFileInfo.STOP) {
            rtcAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
            BaseRTCAudioLiveRoom.sharedInstance().playAudioEffect(sourceId, rtcAudioFileInfo.file.getAbsolutePath(), 1, true);
        }
        BaseRTCAudioLiveRoom.sharedInstance().setAudioEffectVolume(sourceId, rtcAudioFileInfo.volume);
    }

    /**
     * 刷新背景乐播放状态
     */
    private void flushBgmState(RtcAudioFileInfo rtcAudioFileInfo) {
        if (mCurrAudioFileInfo != rtcAudioFileInfo && mCurrAudioFileInfo != null) {
            mCurrAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
            int index = mBgmFils.indexOf(mCurrAudioFileInfo);
            if (index != -1) {
                BaseRTCAudioLiveRoom.sharedInstance().stopAudioAccompany();
            }
        }
        mCurrAudioFileInfo = rtcAudioFileInfo;
        int playState = rtcAudioFileInfo.playState;
        int prePlaySate = rtcAudioFileInfo.prePlayState;
        if (prePlaySate == RtcAudioFileInfo.PLAYING) {
            BaseRTCAudioLiveRoom.sharedInstance().startAudioAccompany(rtcAudioFileInfo.file.getAbsolutePath(), true, false, 1);
        } else if (playState == RtcAudioFileInfo.STOP || prePlaySate == RtcAudioFileInfo.STOP) {
            BaseRTCAudioLiveRoom.sharedInstance().stopAudioAccompany();
        } else if (playState == RtcAudioFileInfo.PLAYING) {
            BaseRTCAudioLiveRoom.sharedInstance().startAudioAccompany(rtcAudioFileInfo.file.getAbsolutePath(), false, false, 1);
        }
        BaseRTCAudioLiveRoom.sharedInstance().setAudioAccompanyVolume(rtcAudioFileInfo.volume);
    }

    /**
     * 体验时间结束
     */
    private void showTimeoutDialog() {
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();

        mRtcDialogHelper.setTitle(getString(R.string.alirtc_audioliveroom_string_experience_time_out));
        mRtcDialogHelper.setTipsTitle(getString(R.string.alirtc_audioliveroom_string_experience_time_out_please_try_angin));
        mRtcDialogHelper.setConfirmText(getString(R.string.alirtc_audioliveroom_string_know));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                finish();
            }
        });

        mRtcDialogHelper.showCustomTipsView(RtcChatActivity.this);
        mRtcDialogHelper.hideCancelText();
    }

    /**
     * 当rtc sdk报错时弹出
     */
    private void showRtcErrorDialog() {
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();

        mRtcDialogHelper.setTitle(getString(R.string.alirtc_audioliveroom_string_title_dialog_tip));
        mRtcDialogHelper.setTipsTitle(getString(R.string.alirtc_audioliveroom_string_error_rtc_normal));
        mRtcDialogHelper.setConfirmText(getString(R.string.alirtc_audioliveroom_string_confrim_btn));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                finish();
            }
        });

        mRtcDialogHelper.showCustomTipsView(RtcChatActivity.this);
        mRtcDialogHelper.hideCancelText();
    }

    /**
     * 展示退出dialog
     */
    private void showExitDialog() {
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();

        mRtcDialogHelper.setTitle(getString(R.string.alirtc_audioliveroom_string_leave_channel));
        mRtcDialogHelper.setTipsTitle(getString(R.string.alirtc_audioliveroom_string_hint_leave_channel));
        mRtcDialogHelper.setConfirmText(getString(R.string.alirtc_audioliveroom_string_confirm_leave_channel));
        mRtcDialogHelper.setCancelText(getString(R.string.alirtc_audioliveroom_string_continue_to_experience));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                showLoading();
                mRtcDialogHelper.hideAll();
                ThreadUtils.runOnSubThread(new Runnable() {
                    @Override
                    public void run() {
                        BaseRTCAudioLiveRoom.sharedInstance().logout();
                    }
                });
            }
        });

        mRtcDialogHelper.showCustomTipsView(RtcChatActivity.this);
        mRtcDialogHelper.showCancelText();
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    @Override
    public void onWifiTo4G() {

    }

    @Override
    public void on4GToWifi() {

    }

    @Override
    public void onReNetConnected(boolean isReconnect) {

    }

    @Override
    public void onNetUnConnected() {
        Intent intent = new Intent(RtcChatActivity.this, NetWorkErrorActivity.class);
        startActivity(intent);
    }

    private void showLoading() {
        RTCLoadingDialogHelper.getInstance().showLoadingView(RtcChatActivity.this);
    }

    private void hideLoading() {
        RTCLoadingDialogHelper.getInstance().hideLoadingView();
    }

    private int getSeatIndex(String seatIndex) {
        int index = -1;
        if (TextUtils.isEmpty(seatIndex)) {
            return index;
        }
        try {
            index = Integer.parseInt(seatIndex);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return index;
    }
}
