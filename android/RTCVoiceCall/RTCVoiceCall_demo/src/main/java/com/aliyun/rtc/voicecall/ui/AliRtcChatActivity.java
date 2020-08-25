package com.aliyun.rtc.voicecall.ui;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcEngineEventListener;
import com.alivc.rtc.AliRtcEngineImpl;
import com.alivc.rtc.AliRtcEngineNotify;
import com.alivc.rtc.AliRtcRemoteUserInfo;
import com.alivc.rtc.device.utils.StringUtils;
import com.aliyun.rtc.voicecall.R;
import com.aliyun.rtc.voicecall.adapter.BgAdapter;
import com.aliyun.rtc.voicecall.adapter.BgmAdapter;
import com.aliyun.rtc.voicecall.bean.AliUserInfoResponse;
import com.aliyun.rtc.voicecall.bean.ChannelStartTimeResponse;
import com.aliyun.rtc.voicecall.constant.Constant;
import com.aliyun.rtc.voicecall.network.OkHttpCientManager;
import com.aliyun.rtc.voicecall.network.OkhttpClient;
import com.aliyun.rtc.voicecall.utils.BitmapUtil;
import com.aliyun.rtc.voicecall.utils.FileUtil;
import com.aliyun.rtc.voicecall.utils.PermissionUtil;
import com.aliyun.rtc.voicecall.utils.TimeConverterUtil;
import com.aliyun.rtc.voicecall.utils.ToastUtils;
import com.aliyun.rtc.voicecall.utils.UIHandlerUtil;
import com.aliyun.rtc.voicecall.view.AlivcTipDialog;
import com.aliyun.rtc.voicecall.view.TitleBar;
import com.aliyun.svideo.common.utils.NetWatchdogUtils;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.webrtc.alirtcInterface.AliParticipantInfo;
import org.webrtc.alirtcInterface.AliStatusInfo;
import org.webrtc.alirtcInterface.AliSubscriberInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AliRtcChatActivity extends AppCompatActivity implements TitleBar.MenuBtnListener, View.OnClickListener, BgmAdapter.OnPlayBtnClickListener, BgAdapter.OnBgClickListener, TitleBar.BackBtnListener, NetWatchdogUtils.NetChangeListener, PermissionUtil.PermissionGrantedListener {
    private static final String TAG = AliRtcChatActivity.class.getSimpleName();
    private AliRtcEngineImpl mEngine;
    private boolean mEnableSilent;
    /**
     * 我方头像
     */
    private ImageView mIvUser1;
    /**
     * 用户2的头像
     */
    private ImageView mIvUser2;
    /**
     * 静音按钮
     */
    private TextView mTvSilent;
    /**
     * 挂断按钮
     */
    private TextView mTvRingOff;
    /**
     * 扬声器、听筒模式切换按钮
     */
    private TextView mTvLoudSpeaker;
    private String mChannelId;
    private AliUserInfoResponse.AliUserInfo mRtcAuthInfo;
    private DrawerLayout mDrawerLayout;
    private List<File> mFiles = new ArrayList<>();
    private List<Bitmap> mBgBitmaps = new ArrayList<>();
    private BgmAdapter mBgmAdapter;
    private BgAdapter mBgAdapter;
    private TextView mIvUser2Name;
    private static final int TITLE_BAR_EMNU_SETTING_ID = 1111;
    private static final int TITLE_BAR_EMNU_BACK_ID = 11111;
    private LinearLayout mLeftDrawLayout;
    private Pair<File, Boolean> mSelectedBgmData;
    private TextView mTvExperienceTime;
    private TextView mTvWaitting;
    private TextView mTvHintTimeout;
    private ImageButton mIbHintLineClose;
    private LinearLayout mLlHintLine;
    private TimeCountRunnable mTimeCountRunnable;
    private NetWatchdogUtils mNetWatchdogUtils;
    private boolean showUser1etBad, showUser2etBad = false;
    private int currBgm;
    private boolean noAudioPermission = false;
    private String user2Uid;
    private long mUser2LoginTime;
    private TitleBar mTitleBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_voicecall_activity_rtc_chat);
        //getData
        getDataForIntent();
        //初始化view
        initView();
        //初始化view事件
        initEvent();
        //初始化rtc引擎
        initAlivcRtcEngine();
        initBgm();
        checkUserOnline();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int statusBarHeight = getStatusBarHeight();
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mTitleBar.getLayoutParams();
            layoutParams.setMargins(0, statusBarHeight, 0, 0);
            mTitleBar.setLayoutParams(layoutParams);
        }
    }

    /**
     * 获取状态栏高度
     *
     * @return 状态栏高度
     */
    public int getStatusBarHeight() {
        try {
            // 获得状态栏高度
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            return getResources().getDimensionPixelSize(resourceId);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNetWatchdogUtils == null) {
            //添加网络监听
            mNetWatchdogUtils = new NetWatchdogUtils(AliRtcChatActivity.this);
            mNetWatchdogUtils.setNetChangeListener(this);
            mNetWatchdogUtils.startWatch();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //请求录音权限权限
        PermissionUtil.requestPermissions(AliRtcChatActivity.this, new String[] {PermissionUtil.PERMISSION_RECORD_AUDIO}, PermissionUtil.PERMISSION_REQUEST_CODE, AliRtcChatActivity.this);
    }

    //检测用户是否已经加入房间
    private void checkUserOnline() {
        if (mRtcAuthInfo != null) {
            boolean userOnline = mEngine.isUserOnline(mRtcAuthInfo.getUserid());
            if (userOnline) {
                //默认进入房间推流
                startPublish();
            } else {
                joinChannel(mChannelId);
            }
        }

    }

    /**
     * 加载bgm文件
     */
    private void initBgm() {
        ThreadUtils.runOnSubThread(new LoadBgmRunnable());
    }

    /**
     * 加载成功后刷新列表
     */
    private void initBgmList(List<File> files) {
        mFiles.clear();
        mFiles.add(0, null);
        mFiles.addAll(files);
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                if (mBgmAdapter != null) {
                    mBgmAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * 获取intent传过来的用户信息
     */
    private void getDataForIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle b = intent.getExtras();
            if (b != null) {
                //频道号
                mChannelId = b.getString("channel");
                //用户信息
                mRtcAuthInfo = (AliUserInfoResponse.AliUserInfo) b.getSerializable("rtcAuthInfo");
            }
        }
    }

    private void initEvent() {
        mTvSilent.setOnClickListener(this);
        mTvRingOff.setOnClickListener(this);
        mTvLoudSpeaker.setOnClickListener(this);
    }

    /**
     * 初始化rtc引擎
     */
    private void initAlivcRtcEngine() {
        mEngine = AliRtcEngine.getInstance(getApplicationContext());
        mEngine.setAudioOnlyMode(true);
        mEngine.setRtcEngineNotify(mEngineNotify);
        mEngine.setRtcEngineEventListener(mEventListener);
        //是否开启扬声器
        boolean speakerOn = mEngine.isSpeakerOn();
        if (!speakerOn) {
            // true为扬声器模式，false为听筒模式
            //（只能在主线程调用）
            //默认是扬声器状态
            UIHandlerUtil.getInstance().postRunnable(new EnableSpeakerPhoneRunnable(true));
        }
    }

    private void initView() {
        mTitleBar = (TitleBar) findViewById(R.id.alivc_voicecall_title_bar);
        TitleBar leftDrawerLayoutTitleBar = (TitleBar) findViewById(R.id.alivc_voicecall_left_drawlayout_title_bar);
        mIvUser1 = (ImageView) findViewById(R.id.alivc_voicecall_iv_user1);
        mIvUser2 = (ImageView) findViewById(R.id.alivc_voicecall_iv_user2);
        mIvUser2Name = (TextView) findViewById(R.id.alivc_voicecall_tv_user2_name);
        mTvSilent = (TextView) findViewById(R.id.alivc_voicecall_tv_silent);
        mTvRingOff = (TextView) findViewById(R.id.alivc_voicecall_tv_ring_off);
        mTvLoudSpeaker = (TextView) findViewById(R.id.alivc_voicecall_tv_loud_speaker);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.alivc_voicecall_btn_drawerlayout);
        RecyclerView mRcyBgm = (RecyclerView) findViewById(R.id.alivc_voicecall_rcy_bgm);
        RecyclerView mRcyBg = (RecyclerView) findViewById(R.id.alivc_voicecall_rcy_bg);
        mLeftDrawLayout = (LinearLayout) findViewById(R.id.alivc_voicecall_ll_left_drawlayout);
        mTvExperienceTime = (TextView) findViewById(R.id.alivc_voicecall_tv_experience_time);
        mTvWaitting = (TextView) findViewById(R.id.alivc_voicecall_tv_waiting);
        mTvHintTimeout = (TextView) findViewById(R.id.alivc_voicecall_tv_hint_timeout);
        mIbHintLineClose = (ImageButton) findViewById(R.id.alivc_voicecall_ibn_close);
        mLlHintLine = (LinearLayout) findViewById(R.id.alivc_voicecall_ll_hint_line);

        mIbHintLineClose.setOnClickListener(this);

        mTitleBar.setTitle(String.format(getString(R.string.alivc_voicecall_string_title_channel_id), mChannelId));
        mTitleBar.setTitleTextColor(Color.WHITE);
        mTitleBar.setBackBtnListener(this);
        mTitleBar.setMenuIcon(R.mipmap.alivc_voicecall_icon_mine_setting);
        mTitleBar.setMenuBtnId(TITLE_BAR_EMNU_SETTING_ID);
        mTitleBar.setMenuBtnListener(this);

        leftDrawerLayoutTitleBar.setTitle(R.string.alivc_voicecall_string_text_setting);
        leftDrawerLayoutTitleBar.setMenuIcon(R.mipmap.alivc_voice_call_btn_close);
        leftDrawerLayoutTitleBar.setMenuBtnId(TITLE_BAR_EMNU_BACK_ID);
        leftDrawerLayoutTitleBar.setMenuBtnListener(this);

        /**
         * 默认开启扬声器
         */
        mTvLoudSpeaker.setSelected(true);

        refulshUser2View(null, false);
        mIvUser1.setImageBitmap(BitmapUtil.createCircleImage(AliRtcChatActivity.this, R.drawable.alivc_voice_call_icon_user1));
        LinearLayoutManager bgmManager = new LinearLayoutManager(AliRtcChatActivity.this);
        bgmManager.setOrientation(LinearLayout.VERTICAL);
        mRcyBgm.setLayoutManager(bgmManager);
        mBgmAdapter = new BgmAdapter(mFiles, AliRtcChatActivity.this);
        mBgmAdapter.setOnPlayBtnClickListener(this);
        mRcyBgm.setAdapter(mBgmAdapter);

        LinearLayoutManager bgManager = new LinearLayoutManager(AliRtcChatActivity.this);
        bgManager.setOrientation(LinearLayout.HORIZONTAL);
        mRcyBg.setLayoutManager(bgManager);
        mBgBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.alivc_voice_call_bg_setting_1));
        mBgBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.alivc_voice_call_bg_setting_2));
        mBgBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.alivc_voice_call_bg_setting_3));
        mBgAdapter = new BgAdapter(mBgBitmaps, AliRtcChatActivity.this);
        mBgAdapter.setOnBgClickListener(this);
        mRcyBg.setAdapter(mBgAdapter);
        /*
         * 设置默认背景
         */
        mBgAdapter.setSelectedPosition(0);
        mDrawerLayout.setBackground(new BitmapDrawable(mBgBitmaps.get(0)));
        mLeftDrawLayout.setBackground(new BitmapDrawable(mBgBitmaps.get(0)));
        mBgmAdapter.setSelectedPosition(0);
        //设置侧滑菜单为全屏
        ViewGroup.LayoutParams layoutParams = mLeftDrawLayout.getLayoutParams();
        layoutParams.width = ScreenUtils.getWidth(this);
        mLeftDrawLayout.setLayoutParams(layoutParams);
        //禁止手势滑动
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //展示倒计时控件
        //        mTvExperienceTime.setVisibility(View.VISIBLE);
    }


    /**
     * 用户操作回调监听(回调接口都在子线程)
     */
    private AliRtcEngineEventListener mEventListener = new AliRtcEngineEventListener() {

        /**
         * 加入房间的回调
         * @param i 结果码
         */
        @Override
        public void onJoinChannelResult(int i) {
            Log.i(TAG, "onJoinChannelResult: " + i);
            //默认进入房间推流
            startPublish();
            //获取房间创建时间
            getChannelStartTimeByNet();
        }

        /**
         * 离开房间的回调
         * @param i 结果码
         */
        @Override
        public void onLeaveChannelResult(int i) {
            //用户1离开房间
        }

        /**
         * 推流的回调
         * @param i 结果码
         * @param s publishId
         */
        @Override
        public void onPublishResult(int i, String s) {

        }

        /**
         * 取消发布本地流回调
         * @param i 结果码
         */
        @Override
        public void onUnpublishResult(int i) {

        }

        /**
         * 订阅成功的回调
         * @param s userid
         * @param i 结果码
         * @param aliRtcVideoTrack 视频的track
         * @param aliRtcAudioTrack 音频的track
         */
        @Override
        public void onSubscribeResult(String s, int i, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack,
                                      AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack) {
        }

        /**
         * 取消的回调
         * @param i 结果码
         * @param s userid
         */
        @Override
        public void onUnsubscribeResult(int i, String s) {
        }

        /**
         * 网络状态变化的回调
         */
        @Override
        public void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1) {
//            Log.i(TAG, "onNetworkQualityChanged: s --> " + s + ",aliRtcNetworkQuality --> " + aliRtcNetworkQuality + ",liRtcNetworkQuality1 --> " + aliRtcNetworkQuality1);
            if (aliRtcNetworkQuality1.getValue() >= AliRtcEngine.AliRtcNetworkQuality.Network_Bad.getValue() && aliRtcNetworkQuality1.getValue() <= AliRtcEngine.AliRtcNetworkQuality.Network_VeryBad.getValue()) {//网络质量差
                if (StringUtils.equals(mRtcAuthInfo.getUserid(), s) && !showUser1etBad) {//自己
                    showUser1etBad = true;
                    UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showInCenter(AliRtcChatActivity.this, getString(R.string.alivc_voicecall_string_user1_network_not_better));
                        }
                    });
                } else if (!StringUtils.equals(mRtcAuthInfo.getUserid(), s) && !showUser2etBad) { //用户2
                    showUser2etBad = true;
                    UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showInCenter(AliRtcChatActivity.this, getString(R.string.alivc_voicecall_string_user2_network_not_better));
                        }
                    });
                }
            }
        }

        /**
         * 出现警告的回调
         * @param i
         */
        @Override
        public void onOccurWarning(int i) {

        }

        /**
         * 出现错误的回调
         * @param error 错误码
         */
        @Override
        public void onOccurError(final int error) {
            UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                @Override
                public void run() {
                    showRtcErrorDialog(error);
                }
            });
        }

        /**
         * 当前设备性能不足
         */
        @Override
        public void onPerformanceLow() {

        }

        /**
         * 当前设备性能恢复
         */
        @Override
        public void onPermormanceRecovery() {

        }

        /**
         * 连接丢失
         */
        @Override
        public void onConnectionLost() {
            Log.i(TAG, "onConnectionLost: ");
        }

        /**
         * 尝试恢复连接
         */
        @Override
        public void onTryToReconnect() {
            Log.i(TAG, "onTryToReconnect: ");
        }

        /**
         * 连接已恢复
         */
        @Override
        public void onConnectionRecovery() {
            Log.i(TAG, "onConnectionRecovery: ");
        }


    };

    /**
     * 当rtc sdk报错时弹出
     *
     * @param error 错误码
     */
    private void showRtcErrorDialog(int error) {
        AlivcTipDialog alivcTipDialog = new AlivcTipDialog.Builder(AliRtcChatActivity.this)
        .setTitle(getString(R.string.alivc_voicecall_string_title_dialog_tip))
        .setDes(getString(R.string.alivc_voicecall_string_error_rtc_normal))
        .setButtonType(AlivcTipDialog.ONE_BUTTON)
        .setOneBtnStr(getString(R.string.alivc_voicecall_string_confrim_btn))
        .setOneButtonClickListener(new AlivcTipDialog.OneButtonClickListener() {

            @Override
            public void onClicked() {
                finish();
            }
        })
        .create();
        alivcTipDialog.setCanceledOnTouchOutside(false);
        alivcTipDialog.setCancelable(false);
        if (!alivcTipDialog.isShowing()) {
            alivcTipDialog.show();
        }
    }

    /**
     * SDK事件通知(回调接口都在子线程)
     */
    private AliRtcEngineNotify mEngineNotify = new AliRtcEngineNotify() {
        /**
         * 远端用户停止发布通知，处于OB（observer）状态
         * @param aliRtcEngine 核心引擎对象
         * @param s userid
         */
        @Override
        public void onRemoteUserUnPublish(AliRtcEngine aliRtcEngine, String s) {
            Log.i(TAG, "onRemoteUserUnPublish: ");
        }

        /**
         * 远端用户上线通知
         * @param s userid
         */
        @Override
        public void onRemoteUserOnLineNotify(String s) {
            Log.i(TAG, "onRemoteUserOnLineNotify: s --> " + s);
            user2Uid = s;
            mUser2LoginTime = System.currentTimeMillis();
            if (mEngine != null && !TextUtils.isEmpty(s)) {
                final AliRtcRemoteUserInfo userInfo = mEngine.getUserInfo(s);
                //显示user2头像
                UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        refulshUser2View(userInfo, true);
                    }
                });
            }
        }

        /**
         * 远端用户下线通知
         * @param s userid
         */
        @Override
        public void onRemoteUserOffLineNotify(String s) {
            Log.i(TAG, "onRemoteUserOffLineNotify: s --> " + s);
            if (StringUtils.equals(user2Uid, s) && System.currentTimeMillis() - mUser2LoginTime > 1000) {
                user2Uid = "";
                if (mEngine != null && !TextUtils.isEmpty(s)) {
                    final AliRtcRemoteUserInfo userInfo = mEngine.getUserInfo(s);
                    //显示user2头像
                    UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            refulshUser2View(userInfo, false);
                            ToastUtils.showInCenter(AliRtcChatActivity.this, getString(R.string.alivc_voicecall_string_user2_leave_channel));
                        }
                    });
                }
            }
        }

        /**
         * 远端用户发布音视频流变化通知
         * @param s userid
         * @param aliRtcAudioTrack 音频流
         * @param aliRtcVideoTrack 相机流
         */
        @Override
        public void onRemoteTrackAvailableNotify(String s, AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack,
                AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
        }

        /**
         * 订阅流回调，可以做UI及数据的更新
         * @param s userid
         * @param aliRtcAudioTrack 音频流
         * @param aliRtcVideoTrack 相机流
         */
        @Override
        public void onSubscribeChangedNotify(String s, AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack,
                                             AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {

        }

        /**
         * 订阅信息
         * @param aliSubscriberInfos 订阅自己这边流的user信息
         * @param i 当前订阅人数
         */
        @Override
        public void onParticipantSubscribeNotify(AliSubscriberInfo[] aliSubscriberInfos, int i) {

        }

        /**
         * 首帧的接收回调
         * @param s callId
         * @param s1 stream_label
         * @param s2 track_label 分为video和audio
         * @param i 时间
         */
        @Override
        public void onFirstFramereceived(String s, String s1, String s2, int i) {
        }

        /**
         * 首包的发送回调
         * @param s callId
         * @param s1 stream_label
         * @param s2 track_label 分为video和audio
         * @param i 时间
         */
        @Override
        public void onFirstPacketSent(String s, String s1, String s2, int i) {
        }

        /**
         *首包数据接收成功
         * @param callId 远端用户callId
         * @param streamLabel 远端用户的流标识
         * @param trackLabel 远端用户的媒体标识
         * @param timeCost 耗时
         */
        @Override
        public void onFirstPacketReceived(String callId, String streamLabel, String trackLabel, int timeCost) {

        }

        /**
         * 取消订阅信息回调
         * @param aliParticipantInfos 订阅自己这边流的user信息
         * @param i 当前订阅人数
         */
        @Override
        public void onParticipantUnsubscribeNotify(AliParticipantInfo[] aliParticipantInfos, int i) {

        }

        /**
         * 被服务器踢出或者频道关闭时回调
         * @param i
         */
        @Override
        public void onBye(int i) {
            Log.i(TAG, "onBye: " + i);
            //频道关闭，体验时间结束
            if (i == 2) {
                //暂停计时
                if (mTimeCountRunnable != null) {
                    mTimeCountRunnable.setLoop(false);
                }
                UIHandlerUtil.getInstance().postRunnable(new ShowTimeOutDialogRunnable());
            }
        }

        @Override
        public void onParticipantStatusNotify(AliStatusInfo[] aliStatusInfos, int i) {

        }

    };

    /**
     * 体验时间结束
     */
    private void showTimeoutDialog() {
        AlivcTipDialog alivcTipDialog = new AlivcTipDialog.Builder(AliRtcChatActivity.this)
        .setTitle(getString(R.string.alivc_voicecall_string_experience_time_out))
        .setDes(getString(R.string.alivc_voicecall_string_experience_time_out_please_try_angin))
        .setButtonType(AlivcTipDialog.ONE_BUTTON)
        .setOneBtnStr(getString(R.string.alivc_voicecall_string_know))
        .setOneButtonClickListener(new AlivcTipDialog.OneButtonClickListener() {

            @Override
            public void onClicked() {
                finish();
            }
        })
        .create();
        alivcTipDialog.setCanceledOnTouchOutside(false);
        alivcTipDialog.setCancelable(false);
        if (!alivcTipDialog.isShowing()) {
            alivcTipDialog.show();
        }
    }

    /**
     * 设置按钮回调
     *
     * @param id
     */
    @Override
    public void onMenuBtnClicked(int id) {
        switch (id) {
        case TITLE_BAR_EMNU_SETTING_ID:
            drawerRightFrame();
            break;
        case TITLE_BAR_EMNU_BACK_ID:
            closeRightFrame();
            break;
        default:
            break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.alivc_voicecall_tv_silent) {
            togglePublishState();
        } else if (id == R.id.alivc_voicecall_tv_ring_off) {
            finish();
        } else if (id == R.id.alivc_voicecall_tv_loud_speaker) {
            toggleSpeakerOnState();
        } else if (id == R.id.alivc_voicecall_ibn_close) {
            mLlHintLine.setVisibility(View.GONE);
        }
    }

    /*停止音乐和通话并退出房间*/
    private void stopautoAndLeaveChannel() {
        //挂断 停止推流 停止背景音乐 退出房间 finish
        endPublish();
        stopBgm();
        leaveChannel();
        //终止线程
        if (mTimeCountRunnable != null) {
            mTimeCountRunnable.setLoop(false);
        }
    }

    /**
     * 停止背景音乐
     */
    private void stopBgm() {
        if (mEngine != null) {
            mEngine.stopAudioAccompany();
        }
    }

    /**
     * 切换听筒、扬声器输出
     */
    private void toggleSpeakerOnState() {
        if (mEngine != null) {
            boolean isSpeakerOn = !mEngine.isSpeakerOn();
            //切换听筒、扬声器输出
            UIHandlerUtil.getInstance().postRunnable(new EnableSpeakerPhoneRunnable(isSpeakerOn));
            changeLoudSpeakerState(isSpeakerOn);
        }
    }

    /**
     * 改变是否推流状态
     */
    private void togglePublishState() {
        //静音 停止推流
        if (mEnableSilent) {//当前是静音状态变成播放状态
            startPublish();
        } else {
            endPublish();
        }
        mEnableSilent = !mEnableSilent;
        changeSilentBtnState(mEnableSilent);
    }

    /**
     * 根据免提状态刷新页面
     */
    private void changeLoudSpeakerState(boolean b) {
        mTvLoudSpeaker.setSelected(b);
        ToastUtils.showInCenter(AliRtcChatActivity.this, getString(b ? R.string.alivc_voicecall_string_text_loud_speaker_enable : R.string.alivc_voicecall_string_text_loud_speaker_unenable));

    }

    /**
     * 根据静音状态刷新页面
     */
    private void changeSilentBtnState(boolean enableSilent) {
        mTvSilent.setSelected(enableSilent);
        ToastUtils.showInCenter(AliRtcChatActivity.this, getString(enableSilent ? R.string.alivc_voicecall_string_text_silent_enable : R.string.alivc_voicecall_string_text_silent_unenable));
    }

    /**
     * 背景音乐播放按钮被点击
     *
     * @param file 音效文件
     */
    @Override
    public void onPlayBtnClickListener(File file, boolean playing) {
        mSelectedBgmData = new Pair<>(file, playing);
        if (mEngine != null && file != null) {
            /*
             * onlyLocalPlay    是否只本地播放，true：只本地播放；false：本地播放和推流
             * replaceMic   是否替换麦克风采集，true：替换麦克风采集，只有伴奏声；false：与麦克风共存
             * */
            int index = mFiles.indexOf(file);
            if (index != currBgm) {
                mEngine.stopAudioEffect(currBgm);
                currBgm = index;
            }
            if (playing) {
                int i = mEngine.playAudioEffect(currBgm, file.getPath(), -1, false);
                Log.i(TAG, "OnPlayBtnClickListener: 播放背景音乐反馈码 --> " + i);
            } else {
                mEngine.stopAudioEffect(currBgm);
            }
        } else if (mEngine != null) {//选择无音乐
            mEngine.stopAudioEffect(currBgm);
        }
    }

    /**
     * 背景图片选择回调
     *
     * @param bitmap bgm
     */
    @Override
    public void onBgClickListener(Bitmap bitmap) {
        mDrawerLayout.setBackground(new BitmapDrawable(bitmap));
        //        mLeftDrawLayout.setBackground(new BitmapDrawable(bitmap));
    }

    /**
     * titlebar 返回按钮点击事件
     */
    @Override
    public void onBackBtnClicked() {
        AlivcTipDialog alivcTipDialog = new AlivcTipDialog.Builder(AliRtcChatActivity.this)
        .setTitle(getString(R.string.alivc_voicecall_string_leave_channel))
        .setDes(getString(R.string.alivc_voicecall_string_hint_leave_channel))
        .setButtonType(AlivcTipDialog.TWO_BUTTON)
        .setCancelStr(getString(R.string.alivc_voicecall_string_confirm_leave_channel))
        .setConfirmStr(getString(R.string.alivc_voicecall_string_continue_to_experience))
        .setTwoButtonClickListener(new AlivcTipDialog.TwoButtonClickListener() {
            @Override
            public void onCancel() {
                finish();
            }

            @Override
            public void onConfirm() {
//                ToastUtils.showInCenter(AliRtcChatActivity.this, getString(R.string.alivc_voicecall_string_continue_to_experience));
            }
        })
        .create();
        alivcTipDialog.setCanceledOnTouchOutside(false);
        alivcTipDialog.setCancelable(false);
        alivcTipDialog.show();
    }
    /**
     * 切换听筒、扬声器输出
     */
    private class EnableSpeakerPhoneRunnable implements Runnable {
        boolean enableSpeakerPhone;

        public EnableSpeakerPhoneRunnable(boolean enableSpeakerPhone) {
            this.enableSpeakerPhone = enableSpeakerPhone;
        }

        @Override
        public void run() {
            if (mEngine != null) {
                mEngine.enableSpeakerphone(this.enableSpeakerPhone);
            }
        }
    }

    /**
     * 发布本地流
     */
    public void startPublish() {
        if (mEngine == null) {
            return;
        }
        //发布本地流设置
        //true表示允许发布音频流，false表示不允许
        mEngine.configLocalAudioPublish(!noAudioPermission);
        //        //true表示允许发布相机流，false表示不允许
        //        mEngine.configLocalCameraPublish(true);
        //        //true表示允许发布屏幕流，false表示不允许
        //        mEngine.configLocalScreenPublish(true);
        //        //true表示允许发布次要视频流；false表示不允许
        //        mEngine.configLocalSimulcast(true, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
        mEngine.publish();
    }

    /**
     * 取消发布本地流
     */
    public void endPublish() {
        if (mEngine == null) {
            return;
        }
        mEngine.configLocalAudioPublish(false);
        //        mEngine.configLocalCameraPublish(false);
        //        mEngine.configLocalScreenPublish(false);
        //        mEngine.configLocalSimulcast(false, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
        mEngine.publish();
    }

    /**
     * 退出房间
     */
    public void leaveChannel() {
        //        float sdkCode = getSdkCode();
        //
        //        if (sdkCode > 1.7f){
        //            mEngine.leaveChannel();
        //        }
        if (mEngine != null) {
            mEngine.leaveChannel();
        }
    }

    /**
     * 根据远程用户信息刷新user2 view
     *
     * @param userInfo 用户信息
     */
    public void refulshUser2View(AliRtcRemoteUserInfo userInfo, boolean online) {
        if (mIvUser2 != null && mIvUser2Name != null) {
            mIvUser2Name.setVisibility(online ? View.INVISIBLE : View.VISIBLE);
            mIvUser2.setImageBitmap(BitmapUtil.createCircleImage(AliRtcChatActivity.this,
                                    online ? R.drawable.alivc_voice_call_icon_user2 : R.drawable.alivc_voice_call_icon_user2_gray));
        }
//        mTvExperienceTime.setVisibility(online ? View.VISIBLE : View.GONE);
//        mTvWaitting.setVisibility(online ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopautoAndLeaveChannel();
        ToastUtils.cancel();
        UIHandlerUtil.getInstance().clearAllMsgAndRunnable();
        if (mNetWatchdogUtils != null) {
            mNetWatchdogUtils.stopWatch();
            mNetWatchdogUtils.setNetChangeListener(null);
            mNetWatchdogUtils = null;
        }
    }

    /**
     * 打开抽屉控件
     */
    public void drawerRightFrame() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(Gravity.END, true);
            mBgmAdapter.notifyDataSetChanged();
            mBgAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 关闭抽屉控件
     */
    private void closeRightFrame() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(Gravity.END, true);
            //此时播放音效文件
            if (mSelectedBgmData != null && mSelectedBgmData.first != null && mEngine != null) {
                //先关闭之前的音效
                mEngine.stopAudioAccompany();
                mEngine.stopAudioEffect(currBgm);
                //播放当前音效并推送
                mEngine.startAudioAccompany(mSelectedBgmData.first.getPath(), false, false, -1);
            } else if (mSelectedBgmData != null && mSelectedBgmData.first == null) {
                //先关闭之前的音效
                mEngine.stopAudioAccompany();
                mEngine.stopAudioEffect(currBgm);
            }
        }
    }

    private void joinChannel(String channel) {
        List<String> gslb = mRtcAuthInfo.getGslb();
        AliRtcAuthInfo userInfo = new AliRtcAuthInfo();
        userInfo.setConferenceId(channel);//频道ID
        userInfo.setAppid(mRtcAuthInfo.getAppid());/* 应用ID */
        userInfo.setNonce(mRtcAuthInfo.getNonce());/* 随机码 */
        userInfo.setTimestamp(mRtcAuthInfo.getTimestamp());/* 时间戳*/
        userInfo.setUserId(mRtcAuthInfo.getUserid());/* 用户ID */
        userInfo.setGslb(gslb.toArray(new String[0]));/* GSLB地址*/
        userInfo.setToken(mRtcAuthInfo.getToken());/*鉴权令牌Token*/
        if (mEngine != null) {
            mEngine.joinChannel(userInfo, mRtcAuthInfo.getUserid());/* 用户显示名称 */
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.END)) {
            mDrawerLayout.closeDrawer(Gravity.END);
        } else {
            onBackBtnClicked();
        }
    }

    /**
     * 刷新计时控件
     *
     * @param minute 分
     * @param second 秒
     */
    public void reflushExperienceTimeView(final int minute, final int second) {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                if (mTvExperienceTime.getVisibility() != View.VISIBLE && !TextUtils.isEmpty(user2Uid)) {
                    mTvExperienceTime.setVisibility(View.VISIBLE);
                    mTvWaitting.setVisibility(View.INVISIBLE);
                } else if (TextUtils.isEmpty(user2Uid)) {
                    mTvExperienceTime.setVisibility(View.INVISIBLE);
                    mTvWaitting.setVisibility(View.VISIBLE);
                }
                mTvExperienceTime.setText(String.format(getString(R.string.alivc_voicecall_string_experience_time_format), minute < 10 ? "0" + minute : String.valueOf(minute), second < 10 ? "0" + second : String.valueOf(second)));
//                if (minute >= 9) {
//                    mTvExperienceTime.setTextColor(Color.RED);
//                }
            }
        });

    }

    private void getChannelStartTimeByNet() {
        String url = Constant.getChannelStartTime();
        Map<String, String> params = createChannelStartTimeParams();
        OkHttpCientManager.getInstance().doGet(url, params, new OkhttpClient.HttpCallBack() {
            @Override
            public void onSuccess(String result) {
                Log.i(TAG, "onSuccess: " + result);
                try {
                    ChannelStartTimeResponse channelStartTimeResponse = new Gson().fromJson(result, ChannelStartTimeResponse.class);
                    if (channelStartTimeResponse != null && channelStartTimeResponse.getData() != null) {
                        long startTimes = TimeConverterUtil.utc2LocalTime(channelStartTimeResponse.getData().getChannelStartTimeUtc(), Constant.UTC_TIME_FORMAT_STRING);
                        //开启倒计时
                        if (mTimeCountRunnable == null) {
                            mTimeCountRunnable = new TimeCountRunnable(startTimes);
                            ThreadUtils.runOnSubThread(mTimeCountRunnable);
                        }
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFaild(String errorMsg) {
                Log.i(TAG, "onFaild: " + errorMsg);
            }
        });
    }


    private Map<String, String> createChannelStartTimeParams() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, mChannelId);
        return params;
    }

    private class LoadBgmRunnable implements Runnable {
        @Override
        public void run() {
            String externalCacheDirPath = FileUtil.getExternalCacheDirPath(AliRtcChatActivity.this, Constant.CACHE_PATH);
            File[] files = FileUtil.getFiles(externalCacheDirPath);
            if (files == null || files.length == 0) {
                List<File> bgms = new ArrayList<>();
                try {
                    String[] list = getAssets().list(Constant.ASSETS_BGM_PATH);
                    if (list == null || list.length == 0) {
                        return;
                    }
                    for (String s : list) {
                        if (!s.endsWith(".mp3")){
                            continue;
                        }
                        InputStream open = getAssets().open(Constant.ASSETS_BGM_PATH + File.separator + s);
                        File bgmFile = FileUtil.writeFile(open, externalCacheDirPath + File.separator + s);
                        bgms.add(bgmFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                initBgmList(bgms);
            } else {
                initBgmList(Arrays.asList(files));
            }
        }
    }

    private class TimeCountRunnable implements Runnable {
        private int mMinute = 0;
        private int mSecond = 0;
        private boolean loop = true;
        private long mStartTime;

        public TimeCountRunnable(long channelStartTimeTs) {
            mStartTime = channelStartTimeTs;
        }

        public void setLoop(boolean loop) {
            this.loop = loop;
        }

        @Override
        public void run() {
            while (loop) {
                SystemClock.sleep(1000);
                long tempTime = System.currentTimeMillis() - mStartTime;
                Date date = new Date(tempTime);
                mSecond = date.getSeconds();
                mMinute = date.getMinutes();
//                if (mMinute >= Constant.EXPERIANCE_TIME_OUT_TIME && loop) {
//                    //本地校验超时时间
//                    loop = false;
//                    UIHandlerUtil.getInstance().postRunnable(new ShowTimeOutDialogRunnable());
//                }
                reflushExperienceTimeView(mMinute, mSecond);
            }
        }
    }

    /**
     * 网络监听
     */
    @Override
    public void onWifiTo4G() {
        Log.i(TAG, "onWifiTo4G: ");
    }

    @Override
    public void on4GToWifi() {
        Log.i(TAG, "on4GToWifi: ");
    }

    @Override
    public void onReNetConnected(boolean isReconnect) {
        if (isReconnect) {
            mIbHintLineClose.setVisibility(View.VISIBLE);
            mTvHintTimeout.setText(R.string.alivc_voicecall_string_hint_timeout);
            mTvHintTimeout.setTextColor(getResources().getColorStateList(R.color.alivc_voicecall_color_text_hint_timeout));
            mLlHintLine.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNetUnConnected() {
        mTvHintTimeout.setText(R.string.alivc_voicecall_string_network_conn_error);
        mTvHintTimeout.setTextColor(getResources().getColorStateList(R.color.color_selector_red));
        mIbHintLineClose.setVisibility(View.GONE);
        mLlHintLine.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPermissionGranted() {
        noAudioPermission = false;
        startPublish();
    }

    /**
     * 无权限
     */
    @Override
    public void onPermissionCancel() {
        noAudioPermission = true;
        startPublish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE) {
            PermissionUtil.requestPermissionsResult(AliRtcChatActivity.this, PermissionUtil.PERMISSION_REQUEST_CODE, permissions, grantResults, AliRtcChatActivity.this);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class ShowTimeOutDialogRunnable implements Runnable {
        @Override
        public void run() {
            showTimeoutDialog();
        }
    }

}
