package com.aliyun.rtc.interactiveclassplayer.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcRemoteUserInfo;
import com.alivc.rtc.device.utils.StringUtils;
import com.aliyun.rtc.interactiveclassplayer.R;
import com.aliyun.rtc.interactiveclassplayer.adapter.AutoItemDecoration;
import com.aliyun.rtc.interactiveclassplayer.adapter.BottomFunctionAdapter;
import com.aliyun.rtc.interactiveclassplayer.adapter.StudentListAdapter;
import com.aliyun.rtc.interactiveclassplayer.bean.AliUserInfoResponse;
import com.aliyun.rtc.interactiveclassplayer.bean.AlivcVideoStreamInfo;
import com.aliyun.rtc.interactiveclassplayer.bean.ChannelNumResponse;
import com.aliyun.rtc.interactiveclassplayer.bean.PlayUrlResponse;
import com.aliyun.rtc.interactiveclassplayer.constant.Constant;
import com.aliyun.rtc.interactiveclassplayer.network.OkHttpCientManager;
import com.aliyun.rtc.interactiveclassplayer.network.OkhttpClient;
import com.aliyun.rtc.interactiveclassplayer.play.LivePlayListener;
import com.aliyun.rtc.interactiveclassplayer.play.PlayHelper;
import com.aliyun.rtc.interactiveclassplayer.play.SimplePlayListener;
import com.aliyun.rtc.interactiveclassplayer.rtc.RtcManager;
import com.aliyun.rtc.interactiveclassplayer.utils.ClipboardUtil;
import com.aliyun.rtc.interactiveclassplayer.utils.ScreenUtil;
import com.aliyun.rtc.interactiveclassplayer.utils.ToastUtils;
import com.aliyun.rtc.interactiveclassplayer.utils.UIHandlerUtil;
import com.aliyun.rtc.interactiveclassplayer.view.AlivcTipDialog;
import com.aliyun.rtc.interactiveclassplayer.view.TitleBar;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.webrtc.sdk.SophonSurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_SDK_INVALID_STATE;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_SESSION_REMOVED;

public class AlivcClassRoomActivity extends AppCompatActivity implements TitleBar.BackBtnListener, TitleBar.MenuBtnListener, BottomFunctionAdapter.FunctionCheckedListener, View.OnClickListener, StudentListAdapter.ItemClickListener {


    private static final String TAG = AlivcClassRoomActivity.class.getSimpleName();
    //横屏时隐藏页面上的view的倒计时时间
    private static final int HIDE_VIEW_TIME = 15;
    private String mChannelId;
    private FrameLayout mContainerView;
    private AliUserInfoResponse.AliUserInfo mRtcAuthInfo;
    private RecyclerView mRcyFunctionViews;
    private ArrayList<Pair<String, Integer>> mFunctions;
    private boolean mMuteLocalMic, mMuteLocalCamera;
    private String mStudentName;
    protected String mRemoteUid;
    private RecyclerView mRcyStudentList;
    private ArrayList<AlivcVideoStreamInfo> mAlivcVideoStreamInfos;
    private StudentListAdapter mStudentListAdapter;
    private ImageButton mIbBack;
    private TextView mTvChannelName;
    private CountDownRunnable mCountDownRunnable;
    private AlivcVideoStreamInfo mDisplayVideoStreamInfo, mRemoteVideoStreamInfo;
    private BottomFunctionAdapter mAdapter;
    private ImageView mIvClassNotBegin;
    private SurfaceView mPlaySurfaceview;
    private boolean isStudentRole = true;
    private ImageButton mIbShare;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ScreenUtil.isLandscapeLayout(this)) {
            ScreenUtil.hideStatusBar(this);
        }
        setContentView(R.layout.alivc_big_interactive_class_activity_rtc_chat);
        getDataForIntent();
        initView();
        studentLogin();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 学生身份登陆，采用播放器播放直播流链接
     */
    public void studentLogin() {
        mContainerView.setVisibility(View.GONE);
        mPlaySurfaceview.setVisibility(View.VISIBLE);
        Map<String, String> params = createPlayUrlParams();
        //初始化
        PlayHelper.getInstance().init();
        PlayHelper.getInstance().setDisplayView(mPlaySurfaceview);
        PlayHelper.getInstance().setLivePlayListener(mSimplePlayListener);
        OkHttpCientManager.getInstance().doGet(Constant.getLivePlayUrl(), params, new OkhttpClient.HttpCallBack() {
            @Override
            public void onSuccess(String result) {
                isStudentRole = true;
                if (TextUtils.isEmpty(result)) {
                    return;
                }
                try {
                    PlayUrlResponse response = new Gson().fromJson(result, PlayUrlResponse.class);
                    if (response.getData() != null && response.getData().getPlayUrl() != null) {
                        PlayHelper.getInstance().setPlayUrl(response.getData().getPlayUrl().getFlv());
                    }
                    PlayHelper.getInstance().prepare();
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

    private Map<String, String> createPlayUrlParams() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, mChannelId);
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_ROLE, Constant.NEW_TOKEN_PARAMS_VALUE_ROLE);
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_USERID, mRtcAuthInfo.getUserid());
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_PLATFORM, Constant.NEW_TOKEN_PARAMS_VALUE_PLATFORM);
        return params;
    }

    /**
     * 教师身份登陆，采用rtc互动模式
     */
    public void teacherLogin() {
        isStudentRole = false;
        mContainerView.setVisibility(View.VISIBLE);
        mPlaySurfaceview.setVisibility(View.GONE);
        RtcManager.getInstance().init();
        RtcManager.getInstance().setRtcEngineEventListener(mRtcEngineEventListener);
        RtcManager.getInstance().setRtcEngineNotify(mRtcEngineNotify);
        AliRtcAuthInfo aliRtcAuthInfo = createAliRtcAuthInfo(mChannelId);
        RtcManager.getInstance().joinChannel(aliRtcAuthInfo, mStudentName);
    }

    private AliRtcAuthInfo createAliRtcAuthInfo(String channel) {
        List<String> gslb = mRtcAuthInfo.getGslb();
        AliRtcAuthInfo userInfo = new AliRtcAuthInfo();
        //频道ID
        userInfo.setConferenceId(channel);
        String appid = mRtcAuthInfo.getAppid();
        /* 应用ID */
        userInfo.setAppid(appid);
        /* 随机码 */
        userInfo.setNonce(mRtcAuthInfo.getNonce());
        /* 时间戳*/
        userInfo.setTimestamp(mRtcAuthInfo.getTimestamp());
        String userid = mRtcAuthInfo != null ? mRtcAuthInfo.getUserid() : "";
        /* 用户ID */
        userInfo.setUserId(userid);
        /* GSLB地址*/
        userInfo.setGslb(gslb.toArray(new String[0]));
        /*鉴权令牌Token*/
        userInfo.setToken(mRtcAuthInfo.getToken());
        return userInfo;
    }

    /**
     * 将本地或远端预览信息添加到学生列表，并刷新显示
     * @param s
     * @param aliRtcVideoTrack
     * @param isLocalStream
     */
    private void addVideoStreamInfo(String s, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack, boolean isLocalStream) {
        AlivcVideoStreamInfo alivcVideoStreamInfo = createAlivcVideoStreamInfo(s, aliRtcVideoTrack, isLocalStream);
        //如果当前学生列表已经存在添加的数据，那就是流的类型变了，刷新预览
        if (!mAlivcVideoStreamInfos.contains(alivcVideoStreamInfo)) {
            mAlivcVideoStreamInfos.add(isLocalStream ? 0 : mAlivcVideoStreamInfos.size(), alivcVideoStreamInfo);
        } else {
            mAlivcVideoStreamInfos.set(mAlivcVideoStreamInfos.indexOf(alivcVideoStreamInfo), alivcVideoStreamInfo);
        }
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                mStudentListAdapter.notifyDataSetChanged();
            }
        });
    }

    /**，本地预览才需要mmuteLocalCamera数据
     * @param s 用户id
     * @param aliRtcVideoTrack 视频流类型
     * @param isLocalStream 是否为本地预览
     * @return 封装的视频流信息
     */
    private AlivcVideoStreamInfo createAlivcVideoStreamInfo(String s, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack, boolean isLocalStream) {
        AliRtcRemoteUserInfo userInfo = RtcManager.getInstance().getUserInfo(s);
        return new AlivcVideoStreamInfo.Builder()
               .setUserId(s)
               .setAliRtcVideoTrack(aliRtcVideoTrack)
               .setUserName(userInfo != null ? userInfo.getDisplayName() : "")
               .setLocalStream(isLocalStream)
               .setMuteLocalCamera(mMuteLocalCamera)
               .setMuteLocalMic(mMuteLocalMic)
               .setAliVideoCanvas(new AliRtcEngine.AliVideoCanvas())
               .build();
    }

    private void initView() {
        mContainerView = findViewById(R.id.alivc_big_interactive_class_fl_Container);
        mRcyFunctionViews = findViewById(R.id.alivc_big_interactive_class_rcy_function_views);
        mRcyStudentList = findViewById(R.id.alivc_big_interactive_class_rcy_student_list);
        mIbBack = findViewById(R.id.alivc_big_interactive_class_landscape_ib_back);
        mIbShare = findViewById(R.id.alivc_big_interactive_class_landscape_ib_share);
        mTvChannelName = findViewById(R.id.alivc_big_interactive_class_tv_landscape_channel_name);
        mIvClassNotBegin = findViewById(R.id.alivc_big_interactive_class_iv_icon_class_not_begin);
        mPlaySurfaceview = findViewById(R.id.alivc_big_interactive_class_surface_play);
        RelativeLayout rlContent = findViewById(R.id.alivc_big_interactive_class_rl_content_class_room);

        initFunctionViews();
        initStudentList();

        if (mIbBack != null) {
            mIbBack.setOnClickListener(this);
        }

        if (mTvChannelName != null) {
            mTvChannelName.setText(String.format(getString(R.string.alivc_biginteractiveclass_string_title_channel_id), mChannelId));
        }

        if (mIbShare != null) {
            mIbShare.setOnClickListener(this);
        }
        rlContent.setOnClickListener(this);
        mStudentListAdapter.setItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启计时线程，一段时间未操作屏幕就隐藏按钮
        if (mCountDownRunnable == null) {
            mCountDownRunnable = new CountDownRunnable();
            ThreadUtils.runOnSubThread(mCountDownRunnable);
        }
        RtcManager.getInstance().startPublish();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 初始化学生列表
     */
    private void initStudentList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRcyStudentList.setLayoutManager(linearLayoutManager);
        if (mAlivcVideoStreamInfos == null) {
            mAlivcVideoStreamInfos = new ArrayList<>();
        }
        mStudentListAdapter = new StudentListAdapter(this, mAlivcVideoStreamInfos);
        mRcyStudentList.setAdapter(mStudentListAdapter);
    }

    /**
     * 初始化底部功能view
     */
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
     * @return 图片资源文件和名称集合
     */
    private ArrayList<Pair<String, Integer>> getFunctions() {
        String[] names;
        if (!ScreenUtil.isLandscapeLayout(this)) {
            names = getResources().getStringArray(R.array.functions_portrait);
        } else {
            names = getResources().getStringArray(R.array.functions_landscape);
        }
        ArrayList<Pair<String, Integer>> bottomBtns = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            bottomBtns.add(new Pair<>(names[i], getBottombtnResId(i)));
        }
        return bottomBtns;
    }

    private Integer getBottombtnResId(int i) {
        int resId = -1;
        switch (i) {
        case 0:
            resId = R.drawable.alivc_biginteractiveclass_mute_mic;
            break;
        case 1:
            resId = R.drawable.alivc_biginteractiveclass_mute_camera;
            break;
        case 2:
            resId = R.drawable.alivc_biginteractiveclass_conn_mic;
            break;
        case 3:
            resId = R.drawable.alivc_biginteractiveclass_rotate_camera;
            break;
        case 4:
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                resId = R.drawable.alivc_biginteractiveclass_leavel_channel;
            } else {
                resId = R.drawable.alivc_biginteractiveclass_user_list;
            }
            break;
        default:
        }
        return resId;
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
                //学生姓名
                mStudentName = b.getString("studentName");
            }
        }
    }

    /**
     * 开始主屏预览，复用sophonsurfaceview，不需要重复创建
     */
    private void startPreview() {
        if (mRemoteVideoStreamInfo == null) {
            return;
        }
        AliRtcEngine.AliVideoCanvas aliVideoCanvas = mRemoteVideoStreamInfo.getAliVideoCanvas();
        if (aliVideoCanvas.view == null) {
            SophonSurfaceView sophonSurfaceView = new SophonSurfaceView(this);
            sophonSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            // true 在最顶层，会遮挡一切view
            sophonSurfaceView.setZOrderOnTop(false);
            //true 如已绘制SurfaceView则在surfaceView上一层绘制。
            sophonSurfaceView.setZOrderMediaOverlay(false);
            aliVideoCanvas.view = sophonSurfaceView;
            //设置渲染模式,一共有四种
            aliVideoCanvas.renderMode = AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeFill;
        }
        //添加LocalView
        mContainerView.removeAllViews();
        mContainerView.addView(aliVideoCanvas.view);
        AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack = mRemoteVideoStreamInfo.getAliRtcVideoTrack();
        aliRtcVideoTrack = aliRtcVideoTrack == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth ? AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen : aliRtcVideoTrack;
        RtcManager.getInstance().setRemoteViewConfig(aliVideoCanvas, mRemoteUid, aliRtcVideoTrack);
    }

    @Override
    public void onBackBtnClicked() {
        onBackPressed();
    }

    /**
     * 展示退出dialog
     */
    private void showExitDialog() {
        AlivcTipDialog alivcTipDialog = new AlivcTipDialog.Builder(AlivcClassRoomActivity.this)
        .setTitle(getString(R.string.alivc_biginteractiveclass_string_leave_channel))
        .setDes(getString(R.string.alivc_biginteractiveclass_string_hint_leave_channel))
        .setButtonType(AlivcTipDialog.TWO_BUTTON)
        .setCancelStr(getString(R.string.alivc_biginteractiveclass_string_confirm_leave_channel))
        .setConfirmStr(getString(R.string.alivc_biginteractiveclass_string_continue_to_experience))
        .setTwoButtonClickListener(new AlivcTipDialog.TwoButtonClickListener() {
            @Override
            public void onCancel() {
                finish();
            }

            @Override
            public void onConfirm() {
            }
        })
        .create();
        alivcTipDialog.setCanceledOnTouchOutside(false);
        alivcTipDialog.setCancelable(false);
        alivcTipDialog.show();
    }

    @Override
    public void onMenuBtnClicked(int id) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在destroy中释放播放器和rtc资源
        RtcManager.getInstance().leaveAndDestroy();
        PlayHelper.getInstance().stop();
        PlayHelper.getInstance().release();
        mCountDownRunnable.quick();
        Log.i(TAG, "onDestroy: ");
    }

    /**
     * 底部功能按钮点击事件回调
     */
    @Override
    public boolean onFunctionChecked(int position) {
        boolean b = true;
        switch (position) {
        //静音
        case 0:
            if (!isStudentRole) {
                mMuteLocalMic = !mMuteLocalMic;
                RtcManager.getInstance().muteLocalMic(mMuteLocalMic);
            } else {
                b = false;
                ToastUtils.showInCenter(AlivcClassRoomActivity.this, getString(R.string.alivc_biginteractiveclass_string_need_set_teacher_role));
            }
            break;
        //摄像头
        case 1:
            if (!isStudentRole) {
                mMuteLocalCamera = !mMuteLocalCamera;
                RtcManager.getInstance().muteLocalCamera(mMuteLocalCamera, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
                //刷新UI
                if (mAlivcVideoStreamInfos == null || mAlivcVideoStreamInfos.size() == 0) {
                    break;
                }
                AlivcVideoStreamInfo build = new AlivcVideoStreamInfo.Builder()
                .setUserId(mRtcAuthInfo.getUserid())
                .build();
                int index = mAlivcVideoStreamInfos.indexOf(build);
                if (index != -1) {
                    mAlivcVideoStreamInfos.get(index).setMuteLocalCamera(mMuteLocalCamera);
                    mStudentListAdapter.notifyItemChanged(index);
                } else if (StringUtils.equals(mDisplayVideoStreamInfo.getUserId(), mRtcAuthInfo.getUserid())) {
                    //刷新大屏
                    if (!mMuteLocalCamera) {
                        RtcManager.getInstance().startPreview();
                    } else {
                        RtcManager.getInstance().stopPreview();
                    }
                    boolean isExist = mContainerView.getChildCount() > 0 && mContainerView.getChildAt(0) instanceof SurfaceView;
                    if (isExist) {
                        mContainerView.getChildAt(0).setBackgroundColor(!mMuteLocalCamera ? Color.TRANSPARENT : Color.BLACK);
                    }
                }
            } else {
                b = false;
                ToastUtils.showInCenter(AlivcClassRoomActivity.this, getString(R.string.alivc_biginteractiveclass_string_need_set_teacher_role));
            }
            break;
        //连麦
        case 2:
            toogleClientRole();
            break;
        //翻转
        case 3:
            if (!isStudentRole) {
                RtcManager.getInstance().switchCamera();
            } else {
                b = false;
                ToastUtils.showInCenter(AlivcClassRoomActivity.this, getString(R.string.alivc_biginteractiveclass_string_need_set_teacher_role));
            }
            break;
        //成员列表
        case 4:
            //全屏模式为退出课程
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                showExitDialog();
            }
            break;
        default:
        }
        return b;
    }

    //清除本地视频流的摄像头、静音属性
    private void clearLocalVideoStreamInfo() {
        mMuteLocalMic = mMuteLocalCamera = false;
        //刷新底部按钮
        mAdapter.initBtnStatus();
    }

    /**
     * 切换学生下上麦状态
     */
    private void toogleClientRole() {
        if (!isStudentRole) {
            //清除底部按钮状态
            clearLocalVideoStreamInfo();
            mDisplayVideoStreamInfo = null;
            //1.参照主播流程停⽌拉流
            RtcManager.getInstance().leaveAndDestroy();
            //以学生身份入会
            studentLogin();
            //清除小屏
            if (mAlivcVideoStreamInfos != null && mAlivcVideoStreamInfos.size() > 0) {
                mAlivcVideoStreamInfos.clear();
                //刷新学生列表
                mStudentListAdapter.notifyDataSetChanged();
            }
        } else {
            //1.参照观众流程停⽌拉流
            //2.销毁engine AliRtcEngine::destroy() ;
            //            RtcManager.getInstance().destory();
            //以教师身份入会
            getChannelUserNum();
        }
    }

    private RtcManager.SimpleRtcEngineEventListener mRtcEngineEventListener = new RtcManager.SimpleRtcEngineEventListener() {

        /**
         *
         * @param result 加入房间的结果码，0为成功，反之失败
         */
        @Override
        public void onJoinChannelResult(int result) {
            super.onJoinChannelResult(result);
            if (result == 0) {
                //将本地画面添加到小屏
                addVideoStreamInfo(mRtcAuthInfo.getUserid(), AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera, true);
            } else {
                UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.initAllBtnStatus();
                        ToastUtils.showInCenter(AlivcClassRoomActivity.this, getString(R.string.alivc_biginteractiveclass_string_hint_join_room_faild));
                    }
                });
            }
        }

        /**
         *  当提示下列三个错误时需要重新创建实例
         * @param error 错误码
         */
        @Override
        public void onOccurError(final int error) {
            super.onOccurError(error);
            switch (error) {
            case ERR_SDK_INVALID_STATE:
            case ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT:
            case ERR_SESSION_REMOVED:
                UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        showRtcErrorDialog(error);
                    }
                });
                break;
            default:
            }
        }

        /**
         *  流变化监听
         * @param uid 用户id
         * @param audioTrack 音频流
         * @param videoTrack 视频流
         */
        @Override
        public void onSubscribeChangedNotify(String uid, AliRtcEngine.AliRtcAudioTrack audioTrack, AliRtcEngine.AliRtcVideoTrack videoTrack) {
            super.onSubscribeChangedNotify(uid, audioTrack, videoTrack);
            Log.i(TAG, "onSubscribeChangedNotify: uid --> " + uid);
        }
    };

    private RtcManager.SimpleAliRtcEngineNotify mRtcEngineNotify = new RtcManager.SimpleAliRtcEngineNotify() {

        /**
         * 远端用户下线通知
         * @param s userid
         */
        @Override
        public void onRemoteUserOffLineNotify(String s) {
            super.onRemoteUserOffLineNotify(s);
            if (mAlivcVideoStreamInfos == null) {
                return;
            }
            //主屏的用户退出了
            if (mDisplayVideoStreamInfo != null && StringUtils.equals(s, mDisplayVideoStreamInfo.getUserId())) {
                AlivcVideoStreamInfo build = new AlivcVideoStreamInfo.Builder()
                .setUserId(mRtcAuthInfo.getUserid())
                .build();
                int index = mAlivcVideoStreamInfos.indexOf(build);
                if (index == -1) {
                    return;
                }
                //大屏显示小屏第一个用户
                final AlivcVideoStreamInfo alivcVideoStreamInfo = mAlivcVideoStreamInfos.get(index);
                //小屏删除第一个试图
                //移除成功刷新页面
                removeStudentList(index);
                UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        reflushContainerView(alivcVideoStreamInfo);
                    }
                });
                return;
            }

            AlivcVideoStreamInfo build = new AlivcVideoStreamInfo.Builder()
            .setUserId(s)
            .build();
            int index = mAlivcVideoStreamInfos.indexOf(build);
            //移除成功刷新页面
            if (index != -1) {
                removeStudentList(index);
            }

        }

        /**
         * 远端用户发布音视频流变化通知
         *
         * @param uid                userid
         * @param aliRtcAudioTrack 音频流
         * @param aliRtcVideoTrack 相机流
         */
        @Override
        public void onRemoteTrackAvailableNotify(String uid, AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
            super.onRemoteTrackAvailableNotify(uid, aliRtcAudioTrack, aliRtcVideoTrack);
            Log.i(TAG, "onSubscribeChangedNotify: uid --> " + uid);
            //无流的时候不需要处理
            if (aliRtcVideoTrack == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackNo || aliRtcAudioTrack == AliRtcEngine.AliRtcAudioTrack.AliRtcAudioTrackNo) {
                return;
            }
            if (TextUtils.isEmpty(mRemoteUid) || mDisplayVideoStreamInfo == null || (mRemoteUid.equals(uid) && StringUtils.equals(uid, mDisplayVideoStreamInfo.getUserId()))) {
                mRemoteUid = uid;
                RtcManager.getInstance().configRemoteCameraTrack(uid, true, true);
                mRemoteVideoStreamInfo = mDisplayVideoStreamInfo = createAlivcVideoStreamInfo(uid, aliRtcVideoTrack, false);
                UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        startPreview();
                    }
                });
            } else if (!StringUtils.equals(mDisplayVideoStreamInfo.getUserId(), uid)) {
                addVideoStreamInfo(uid, aliRtcVideoTrack, false);
                RtcManager.getInstance().configRemoteCameraTrack(uid, false, true);

            }
        }

        /**
         * 被服务器踢出或者频道关闭时回调
         * @param i 状态码
         */
        @Override
        public void onBye(int i) {
            Log.i(TAG, "onBye: " + i);
            //频道关闭，体验时间结束
            if (i == 2) {
                UIHandlerUtil.getInstance().postRunnable(new ShowTimeOutDialogRunnable());
            }
        }
    };

    private void removeStudentList(final int index) {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                AlivcVideoStreamInfo remove = mAlivcVideoStreamInfos.remove(index);
                if (remove != null) {
                    mStudentListAdapter.detachedPreview(remove);
                    mStudentListAdapter.notifyItemRemoved(index);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.alivc_big_interactive_class_landscape_ib_back) {
            showExitDialog();
        } else if (id == R.id.alivc_big_interactive_class_rl_content_class_room) {
            if (mCountDownRunnable.mCountdown >= 15) {
                showLandscapeLayoutView();
                mCountDownRunnable.resetCount();
            } else {
                hideLandscapeLayoutView();
                mCountDownRunnable.setCount(HIDE_VIEW_TIME);
            }
        } else if (id == R.id.alivc_big_interactive_class_landscape_ib_share) {
            boolean copy = ClipboardUtil.copy(AlivcClassRoomActivity.this, Constant.getWebPlayUrl(mChannelId));
            ToastUtils.showInCenter(AlivcClassRoomActivity.this, getString(copy ? R.string.alivc_biginteractiveclass_string_copy_success : R.string.alivc_biginteractiveclass_string_copy_faild));
        }
    }

    /**
     * 控制横屏时，底部功能按钮和上方的title
     */
    private void showLandscapeLayoutView() {
        if (mIbBack != null) {
            mIbBack.setVisibility(View.VISIBLE);
        }
        if (mTvChannelName != null) {
            mTvChannelName.setVisibility(View.VISIBLE);
        }
        if (mIbShare != null) {
            mIbShare.setVisibility(View.VISIBLE);
        }
        mRcyFunctionViews.setVisibility(View.VISIBLE);
    }

    /**
     * 学生列表的点击时间
     */
    @Override
    public void onItemClicked(int position) {
        if (position >= mAlivcVideoStreamInfos.size() || position < 0) {
            return;
        }
        AlivcVideoStreamInfo alivcVideoStreamInfo = mAlivcVideoStreamInfos.get(position);
        //将正在显示的id设置给小屏
        if (mDisplayVideoStreamInfo != null) {
            mAlivcVideoStreamInfos.set(position, mDisplayVideoStreamInfo);
            //小屏切换到大流
            RtcManager.getInstance().configRemoteCameraTrack(mDisplayVideoStreamInfo.getUserId(), false, true);
            mDisplayVideoStreamInfo = alivcVideoStreamInfo;
            mStudentListAdapter.notifyItemChanged(position);
        }
        //刷新大屏
        reflushContainerView(alivcVideoStreamInfo);
    }

    /**
     * 刷新主屏，切换两个流展示的spoonsurface即可，不需要重复预览
     * @param alivcVideoStreamInfo 流信息
     */
    private void reflushContainerView(AlivcVideoStreamInfo alivcVideoStreamInfo) {
        //主屏切换到大流
        RtcManager.getInstance().configRemoteCameraTrack(alivcVideoStreamInfo.getUserId(), true, true);
        AliRtcEngine.AliVideoCanvas aliVideoCanvas = alivcVideoStreamInfo.getAliVideoCanvas();
        if (aliVideoCanvas != null && aliVideoCanvas.view != null) {
            // true 在最顶层，会遮挡一切view
            aliVideoCanvas.view.setZOrderOnTop(false);
            //true 如已绘制SurfaceView则在surfaceView上一层绘制。
            aliVideoCanvas.view.setZOrderMediaOverlay(false);
            mContainerView.removeAllViews();
            mContainerView.addView(aliVideoCanvas.view);
        }
    }

    /**
     * 计时线程，控制页面按钮的显示隐藏
     */
    private class CountDownRunnable implements Runnable {
        private int mCountdown = 0;
        private boolean startLoop = true;

        @Override
        public void run() {

            while (startLoop) {
                if (HIDE_VIEW_TIME - mCountdown == 0) {
                    hideLandscapeLayoutView();
                }
                SystemClock.sleep(1000);
                mCountdown++;
            }
        }

        private void resetCount() {
            mCountdown = 0;
        }

        private void quick() {
            startLoop = false;
        }

        public void setCount(int i) {
            mCountdown = i;
        }
    }

    private void hideLandscapeLayoutView() {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                if (mIbBack != null) {
                    mIbBack.setVisibility(View.GONE);
                }
                if (mTvChannelName != null) {
                    mTvChannelName.setVisibility(View.GONE);
                }
                if (mIbShare != null) {
                    mIbShare.setVisibility(View.GONE);
                }
                mRcyFunctionViews.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 播放器的监听事件
     */
    private LivePlayListener mSimplePlayListener = new SimplePlayListener() {
        //首帧渲染显示事件
        @Override
        public void onRenderingStart() {
            UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                @Override
                public void run() {
                    mIvClassNotBegin.setVisibility(View.GONE);
                }
            });
        }

        /**
         * loading超时,可能是老师已经退出房间
         */
        @Override
        public void loadingTimeOut() {
            super.loadingTimeOut();
            UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                @Override
                public void run() {
                    showLoadingTimeOutDialog();
                }
            });
        }
    };

    /**
     * 播放器超时回调后展示的dialog
     */
    private void showLoadingTimeOutDialog() {
        AlivcTipDialog alivcTipDialog = new AlivcTipDialog.Builder(AlivcClassRoomActivity.this)
        .setTitle(getString(R.string.alivc_biginteractiveclass_string_title_dialog_tip))
        .setDes(getString(R.string.alivc_biginteractiveclass_string_teacher_is_leavel_room))
        .setButtonType(AlivcTipDialog.ONE_BUTTON)
        .setOneBtnStr(getString(R.string.alivc_biginteractiveclass_string_confrim_btn))
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

    private class ShowTimeOutDialogRunnable implements Runnable {
        @Override
        public void run() {
            showTimeoutDialog();
        }
    }

    /**
     * 体验时间结束
     */
    private void showTimeoutDialog() {
        AlivcTipDialog alivcTipDialog = new AlivcTipDialog.Builder(AlivcClassRoomActivity.this)
        .setTitle(getString(R.string.alivc_biginteractiveclass_string_experience_time_out))
        .setDes(getString(R.string.alivc_biginteractiveclass_string_experience_time_out_please_try_angin))
        .setButtonType(AlivcTipDialog.ONE_BUTTON)
        .setOneBtnStr(getString(R.string.alivc_biginteractiveclass_string_know))
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
     * 当rtc sdk报错时弹出
     *
     * @param error 错误码
     */
    private void showRtcErrorDialog(int error) {
        AlivcTipDialog alivcTipDialog = new AlivcTipDialog.Builder(AlivcClassRoomActivity.this)
        .setTitle(getString(R.string.alivc_biginteractiveclass_string_title_dialog_tip))
        .setDes(getString(R.string.alivc_biginteractiveclass_string_error_rtc_normal))
        .setButtonType(AlivcTipDialog.ONE_BUTTON)
        .setOneBtnStr(getString(R.string.alivc_biginteractiveclass_string_confrim_btn))
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
     * 获取频道人数信息
     */
    private void getChannelUserNum() {
        String url = Constant.getChannelUserNumUrl();
        Map<String, String> params = createChannelNumParams();
        OkHttpCientManager.getInstance().doGet(url, params, new OkhttpClient.HttpCallBack() {
            @Override
            public void onSuccess(String result) {
                try {
                    ChannelNumResponse channelNumResponse = new Gson().fromJson(result, ChannelNumResponse.class);
                    if (channelNumResponse != null && channelNumResponse.getData() != null && channelNumResponse.getData().getUserList().size() > 0) {
                        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                PlayHelper.getInstance().stop();
                                teacherLogin();
                            }
                        });
                    } else {
                        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showInCenter(AlivcClassRoomActivity.this, getString(R.string.alivc_biginteractiveclass_string_teacher_is_leavel_room));
                                mAdapter.initAllBtnStatus();
                            }
                        });
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFaild(String errorMsg) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
        RtcManager.getInstance().stopPublish();
    }

    private Map<String, String> createChannelNumParams() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, mChannelId);
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_PLATFORM, Constant.NEW_TOKEN_PARAMS_VALUE_PLATFORM);
        return params;
    }

    /**
     * 适配部分机型点击音量按键控制的时通话音量
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
            if (mAudioManager != null) {
                mAudioManager.adjustStreamVolume(isStudentRole ? AudioManager.STREAM_MUSIC : AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            }
            break;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            if (mAudioManager != null) {
                mAudioManager.adjustStreamVolume(isStudentRole ? AudioManager.STREAM_MUSIC : AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }
            break;
        default:
        }
        return super.onKeyDown(keyCode, event);
    }
}
