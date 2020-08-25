package com.aliyun.rtc.audiochatroom.rtc;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcEngineEventListener;
import com.alivc.rtc.AliRtcEngineNotify;
import com.alivc.rtc.AliRtcRemoteUserInfo;
import com.alivc.rtc.device.utils.StringUtils;
import com.aliyun.rtc.audiochatroom.api.BaseRTCAuthInfoLoader;
import com.aliyun.rtc.audiochatroom.api.BaseSeatInfoLoader;
import com.aliyun.rtc.audiochatroom.api.impl.AudioLiveRoomModelFactoryImpl;
import com.aliyun.rtc.audiochatroom.bean.AudioRoomUserInfoResponse;
import com.aliyun.rtc.audiochatroom.bean.SeatInfo;
import com.aliyun.rtc.audiochatroom.bean.SeatListInfo;
import com.aliyun.rtc.audiochatroom.api.net.OkhttpClient;
import com.aliyun.rtc.audiochatroom.utils.ApplicationContextUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.ali.Logging;
import org.webrtc.alirtcInterface.AliStatusInfo;

import java.util.ArrayList;
import java.util.List;

public class RTCAudioLiveRoomImpl extends BaseRTCAudioLiveRoom implements AliRtcEngine.AliRtcAudioVolumeObserver {

    private static final String TAG = RTCAudioLiveRoomImpl.class.getSimpleName();
    private AliRtcEngine mEngine;
    private BaseSeatInfoLoader mSeatInfoLoader;
    private ArrayList<SeatInfo> mSeatInfos;
    private static BaseRTCAudioLiveRoom mInstance;
    private RTCAudioLiveRoomDelegate mRTCAudioLiveRoomDelegate;
    private String mChannelId;
    private String mUserName;
    private Handler mUiHandler;
    private final Object mRtcUserInfoLock = new Object(), mAudioVolumeChangeLock = new Object();
    private String mLocalUserId;
    private final BaseRTCAuthInfoLoader mRtcAuthInfoLoader;
    public static final int JOIN_CHANNEL_FAILD_CODE_BY_BAD_NETWORK = -1;

    private RTCAudioLiveRoomImpl() {
        AudioLiveRoomModelFactoryImpl modelFactory = new AudioLiveRoomModelFactoryImpl();
        mSeatInfoLoader = modelFactory.createSeatInfoLoader();
        mRtcAuthInfoLoader = modelFactory.createRTCAuthInfoLoader();
        mSeatInfos = new ArrayList<>();
        mUiHandler = new Handler(Looper.getMainLooper());
        if (mEngine == null) {
            JSONObject jsonObject = new JSONObject();
            try {
                //配置音质模式
                jsonObject.put("user_specified_engine_mode", "ENGINE_HIGH_QUALITY_MODE");
                //配置场景模式
                jsonObject.put("user_specified_scene_mode", "SCENE_MUSIC_MODE");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEngine = AliRtcEngine.getInstance(ApplicationContextUtil.getAppContext());
                        //设置纯音频模式
                        mEngine.setAudioOnlyMode(true);
                        //默认开启扬声器
                        mEngine.enableSpeakerphone(true);
                        //设置频道模式为互动模式
                        mEngine.setChannelProfile(AliRtcEngine.AliRTCSDK_Channel_Profile.AliRTCSDK_Interactive_live);
                        //设置自动订阅，不自动发布
                        mEngine.setAutoPublishSubscribe(false, true);
                        //设置监听
                        mEngine.setRtcEngineEventListener(mRtcEngineEventListener);
                        mEngine.setRtcEngineNotify(mRtcEngineNotify);
                        mEngine.setPlayoutVolume(130);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static BaseRTCAudioLiveRoom sharedInstance() {
        if (mInstance == null) {
            synchronized (RTCAudioLiveRoomImpl.class) {
                if (mInstance == null) {
                    mInstance = new RTCAudioLiveRoomImpl();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void login(String channelId, String userName, AliRtcEngine.AliRTCSDK_Client_Role role) {
        mChannelId = channelId;
        mUserName = userName;
        mEngine.setClientRole(role);
        joinChannel(channelId, userName);
    }

    @Override
    public void logout() {
        mEngine.setAudioEffectVoiceChangerMode(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_OFF);
        mEngine.setAudioEffectReverbMode(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Off);
        mEngine.unRegisterAudioVolumeObserver();
        leaveChannel();
    }

    @Override
    public void setAudioEffectVolume(int soundId, int volume) {
        mEngine.setAudioEffectPlayoutVolume(soundId, volume);
        mEngine.setAudioEffectPublishVolume(soundId, volume);
    }

    @Override
    public void setAudioAccompanyVolume(int volume) {
        mEngine.setAudioAccompanyPublishVolume(volume);
        mEngine.setAudioAccompanyPlayoutVolume(volume);
    }

    /**
     * 上麦需要先判断连麦人数是否超过最大数量
     */
    @Override
    public void enterSeat() {
        setClientRole(AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_Interactive);
    }

    /**
     * 下麦
     */
    @Override
    public void leaveSeat() {
        //1.停止推流
        stopPublish();
    }

    @Override
    public int muteLocalMic(boolean muteLocalMic) {
        return mEngine.muteLocalMic(muteLocalMic);
    }

    @Override
    public int muteAllRemoteAudioPlaying(boolean enable) {
        return mEngine.muteAllRemoteAudioPlaying(enable);
    }

    @Override
    public int enableEarBack(boolean enableEarBack) {
        return mEngine.enableEarBack(enableEarBack);
    }

    @Override
    public void startAudioAccompany(String fileName, boolean onlyLocalPlay, boolean replaceMic, int loopCycles) {
        mEngine.startAudioAccompany(fileName, onlyLocalPlay, replaceMic, loopCycles);
    }

    @Override
    public void stopAudioAccompany() {
        mEngine.stopAudioAccompany();
    }

    @Override
    public void destorySharedInstance() {
        destory();
        mInstance = null;
        mUiHandler.removeCallbacksAndMessages(null);
        mUiHandler = null;
    }

    @Override
    public void playAudioEffect(int soundId, String filePath, int cycles, boolean publish) {
        mEngine.playAudioEffect(soundId, filePath, cycles, publish);
    }

    @Override
    public void stopAudioEffect(int soundId) {
        mEngine.stopAudioEffect(soundId);
    }

    @Override
    public void setAudioEffectReverbMode(AliRtcEngine.AliRtcAudioEffectReverbMode aliRtcAudioEffectReverbMode) {
        mEngine.setAudioEffectReverbMode(aliRtcAudioEffectReverbMode);
    }

    @Override
    public void setRTCAudioLiveRoomDelegate(RTCAudioLiveRoomDelegate audioLiveRoomDelegate) {
        this.mRTCAudioLiveRoomDelegate = audioLiveRoomDelegate;
        if (mRTCAudioLiveRoomDelegate != null) {
            renotifySeatsInfo();
        }
    }

    @Override
    public int setAudioEffectVoiceChangerMode(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode mode) {
        return mEngine == null ? -1 : mEngine.setAudioEffectVoiceChangerMode(mode);
    }

    public void renotifySeatsInfo() {
        synchronized (mRtcUserInfoLock) {
            if (mSeatInfos != null && mRTCAudioLiveRoomDelegate != null) {
                for (SeatInfo seatInfo : mSeatInfos) {
                    mRTCAudioLiveRoomDelegate.onEnterSeat(seatInfo);
                }
            }
        }
    }

    /**
     * sdk音量回调。回调比较频繁
     *
     * @param list 用户音量数据集
     * @param i    混合音量
     */
    @Override
    public void onAudioVolume(List<AliRtcEngine.AliRtcAudioVolume> list, int i) {
        synchronized (mAudioVolumeChangeLock) {
            for (final AliRtcEngine.AliRtcAudioVolume aliRtcAudioVolume : list) {
                //0代表的是自己
                String uid = StringUtils.equals(aliRtcAudioVolume.mUserId, "0") ? mLocalUserId : aliRtcAudioVolume.mUserId;
                SeatInfo seatInfo = findSeatInfoById(uid);
                final boolean isSpeaking = aliRtcAudioVolume.mSpeechstate == 1;
                //没有就新建一个seat对象
                if (seatInfo == null) {
                    updateSeatInfoBySpeakState(uid, isSpeaking);
                    continue;
                }
                boolean oldSpeechState = seatInfo.isSpeaking();
                seatInfo.setSpeaking(isSpeaking);
                //前后状态不一致就回调到UI层处理
                if (oldSpeechState != isSpeaking && mRTCAudioLiveRoomDelegate != null) {
                    final int seatIndex = getSeatIndex(seatInfo.getSeatIndex());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRTCAudioLiveRoomDelegate.onSeatVolumeChanged(seatIndex, isSpeaking);
                        }
                    });
                }
            }
        }
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

    /**
     * SDK事件通知(回调接口都在子线程)
     */
    private AliRtcEngineNotify mRtcEngineNotify = new AliRtcEngineNotify() {

        /**
         * 远端用户上线通知
         *
         * @param s userid
         */
        @Override
        public void onRemoteUserOnLineNotify(String s) {
            Logging.d(TAG, "onRemoteUserOnLineNotify: s --> " + s);
        }

        /**
         * 远端用户下线通知
         *
         * @param s userid
         */
        @Override
        public void onRemoteUserOffLineNotify(String s) {
            Logging.d(TAG, "onRemoteUserOffLineNotify: s --> " + s);
            getSeatList();
        }

        /**
         * 远端用户发布音视频流变化通知
         *
         * @param s                userid
         * @param aliRtcAudioTrack 音频流
         * @param aliRtcVideoTrack 相机流
         */
        @Override
        public void onRemoteTrackAvailableNotify(String s, AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack,
                                                 AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
            Logging.d(TAG, "onRemoteTrackAvailableNotify: s --> " + s);
            if (aliRtcAudioTrack == AliRtcEngine.AliRtcAudioTrack.AliRtcAudioTrackNo) {
                SystemClock.sleep(2000);
            }
            getSeatList();
        }

        /**
         * 首帧的接收回调
         *
         * @param s  callId
         * @param s1 stream_label
         * @param s2 track_label 分为video和audio
         * @param i  时间
         */
        @Override
        public void onFirstFramereceived(String s, String s1, String s2, int i) {
            Logging.d(TAG, "onFirstFramereceived: ");
        }

        /**
         * 首包的发送回调
         *
         * @param s  callId
         * @param s1 stream_label
         * @param s2 track_label 分为video和audio
         * @param i  时间
         */
        @Override
        public void onFirstPacketSent(String s, String s1, String s2, int i) {
            Logging.d(TAG, "onFirstPacketSent: ");
        }

        /**
         * 首包数据接收成功
         *
         * @param callId      远端用户callId
         * @param streamLabel 远端用户的流标识
         * @param trackLabel  远端用户的媒体标识
         * @param timeCost    耗时
         */
        @Override
        public void onFirstPacketReceived(String callId, String streamLabel, String trackLabel, int timeCost) {
            Logging.d(TAG, "onFirstPacketReceived: ");
        }

        /**
         * 被服务器踢出或者频道关闭时回调
         */
        @Override
        public void onBye(int i) {
            Logging.d(TAG, "onBye: ");
            if (i == 2) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //用户被踢出房间（体验时长结束）
                        if (mRTCAudioLiveRoomDelegate != null) {
                            mRTCAudioLiveRoomDelegate.onRoomDestroy();
                        }
                    }
                });
            }
        }

        /**
         * @param uid  用户ID
         * @param mute true：静音，false：未静音
         */
        @Override
        public void onUserAudioMuted(final String uid, final boolean mute) {
            Logging.d(TAG, "onUserAudioMuted: uid : " + uid);
            final SeatInfo seatInfo = findSeatInfoById(uid);
            if (seatInfo == null) {
                updateSeatInfoByMute(uid, mute);
                return;
            }
            seatInfo.setMuteMic(mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRTCAudioLiveRoomDelegate != null) {
                        mRTCAudioLiveRoomDelegate.onSeatMutedChanged(getSeatIndex(seatInfo.getSeatIndex()), mute);
                    }
                }
            });

        }

        @Override
        public void onAudioPlayingStateChanged(final AliRtcEngine.AliRtcAudioPlayingStateCode playState, AliRtcEngine.AliRtcAudioPlayingErrorCode errorCode) {
            super.onAudioPlayingStateChanged(playState, errorCode);
            Logging.d(TAG, "onAudioPlayingStateChanged: audioPlayingStatus : " + playState);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRTCAudioLiveRoomDelegate != null) {
                        mRTCAudioLiveRoomDelegate.onAudioPlayingStateChanged(playState);
                    }
                }
            });
        }

        @Override
        public void onParticipantStatusNotify(AliStatusInfo[] statusInfolist, int count) {
            super.onParticipantStatusNotify(statusInfolist, count);
            for (final AliStatusInfo aliStatusInfo : statusInfolist) {
                final String userId = aliStatusInfo.user_id;
                final boolean mute = aliStatusInfo.status.audio_disabled;
                final SeatInfo seatInfo = findSeatInfoById(userId);
                if (seatInfo == null) {
                    Log.i(TAG, "onParticipantStatusNotify: userid : " + userId + "; mute : " + mute);
                    updateSeatInfoByMute(userId, mute);
                    continue;
                }
                seatInfo.setMuteMic(mute);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mRTCAudioLiveRoomDelegate != null) {
                            mRTCAudioLiveRoomDelegate.onSeatMutedChanged(getSeatIndex(seatInfo.getSeatIndex()), mute);
                        }
                    }
                });
            }
        }
    };

    /**
     * 获取所有麦序
     */
    private void getSeatList() {
        if (TextUtils.isEmpty(mChannelId)) {
            return;
        }
        mSeatInfoLoader.getSeatList(mChannelId, new OkhttpClient.BaseHttpCallBack<SeatListInfo>() {
            @Override
            public void onSuccess(SeatListInfo seatListInfo) {
                Logging.d(TAG, "onSuccess: " + seatListInfo);
                parseSeatListInfo(seatListInfo);
            }

            @Override
            public void onError(String errorMsg) {
                Logging.d(TAG, "onError: " + errorMsg);
            }
        });
    }

    private AliRtcEngineEventListener mRtcEngineEventListener = new AliRtcEngineEventListener() {

        @Override
        public void onJoinChannelResult(int result) {
            Logging.d(TAG, "onJoinChannelResult: result --> " + result);
            joinChannelResult(result);
            mEngine.setVolumeCallbackIntervalMs(160, 3, 1);
            mEngine.registerAudioVolumeObserver(RTCAudioLiveRoomImpl.this);
            //如果是互动角色形式入会，就开始推流
            if (mEngine.getCurrentClientRole() == AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_Interactive) {
                getSeatList();
                startPublish();
            }
        }

        public void onLiveStreamingSignalingResult(int i) {
            Logging.d(TAG, "onLiveStreamingSignalingResult: " + i);
        }

        /**
         * 离开房间的回调
         *
         * @param i 结果码
         */
        @Override
        public void onLeaveChannelResult(final int i) {
            Logging.d(TAG, "onLeaveChannelResult: i --> " + i);
            //清除麦序集合
            mSeatInfos.clear();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRTCAudioLiveRoomDelegate != null) {
                        mRTCAudioLiveRoomDelegate.onLeaveChannelResult(i);
                    }
                }
            });

        }

        /**
         * 网络状态变化的回调
         *
         * @param aliRtcNetworkQuality1 下行网络质量
         * @param aliRtcNetworkQuality  上行网络质量
         * @param s                     String  用户ID
         */
        @Override
        public void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1) {
            if (mRTCAudioLiveRoomDelegate != null) {
                mRTCAudioLiveRoomDelegate.onNetworkQualityChanged(s, aliRtcNetworkQuality, aliRtcNetworkQuality1);
            }
        }

        /**
         * 出现警告的回调
         *
         * @param i 错误码
         */
        @Override
        public void onOccurWarning(int i) {
            Logging.d(TAG, "onOccurWarning: i --> " + i);
        }

        /**
         * 出现错误的回调
         *
         * @param error 错误码
         */
        @Override
        public void onOccurError(final int error) {
            Logging.d(TAG, "onOccurError: error --> " + error);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRTCAudioLiveRoomDelegate != null) {
                        mRTCAudioLiveRoomDelegate.onOccurError(error);
                    }
                }
            });
        }

        /**
         * 当前设备性能不足
         */
        @Override
        public void onPerformanceLow() {
            Logging.d(TAG, "onPerformanceLow: ");
        }

        /**
         * 当前设备性能恢复
         */
        @Override
        public void onPermormanceRecovery() {
            Logging.d(TAG, "onPermormanceRecovery: ");
        }

        /**
         * 连接丢失
         */
        @Override
        public void onConnectionLost() {
            Logging.d(TAG, "onConnectionLost: ");
        }

        /**
         * 尝试恢复连接
         */
        @Override
        public void onTryToReconnect() {
            Logging.d(TAG, "onTryToReconnect: ");
        }

        /**
         * 连接已恢复
         */
        @Override
        public void onConnectionRecovery() {
            Logging.d(TAG, "onConnectionRecovery: ");
        }

        public void onUpdateRoleNotify(final AliRtcEngine.AliRTCSDK_Client_Role oldRole, final AliRtcEngine.AliRTCSDK_Client_Role newRole) {
            Logging.d(TAG, "onUpdateRoleNotify: newRole : " + newRole);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRTCAudioLiveRoomDelegate != null) {
                        mRTCAudioLiveRoomDelegate.onUpdateRoleNotify(oldRole, newRole);
                    }
                }
            });
            //2.切换角色成功后开始推流
            if (newRole == AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_Interactive) {
                startPublish();
            } else {
                SystemClock.sleep(2000);
            }
            getSeatList();
        }

        @Override
        public void onSubscribeChangedNotify(String uid, AliRtcEngine.AliRtcAudioTrack audioTrack, AliRtcEngine.AliRtcVideoTrack videoTrack) {
            super.onSubscribeChangedNotify(uid, audioTrack, videoTrack);
            Logging.d(TAG, "onSubscribeChangedNotify: " + uid);
        }

        /**
         * 推流回调
         *
         * @param result      返回0表示成功，返回其他表示失败
         * @param isPublished true表示推流成功，false表示停止推流
         */
        @Override
        public void onPublishChangedNotify(final int result, final boolean isPublished) {
            super.onPublishChangedNotify(result, isPublished);
            Logging.d(TAG, "onPublishChangedNotify: result : " + result + ", isPublished : " + isPublished);
            if (result == 0 && !isPublished) {
                //2.如果停止推流可以设置用户角色
                setClientRole(AliRtcEngine.AliRTCSDK_Client_Role.AliRTCSDK_live);
            }
            if (mRTCAudioLiveRoomDelegate != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRTCAudioLiveRoomDelegate.onPublishChangedNotify(result, isPublished);
                    }
                });
            }
        }

    };

    private void runOnUiThread(Runnable runnable) {
        if (mUiHandler == null) {
            runnable.run();
            return;
        }
        if (mUiHandler.getLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mUiHandler.post(runnable);
        }
    }

    private void destory() {
        if (mEngine != null) {
            mEngine.destroy();
            mEngine = null;
        }
    }

    private void leaveChannel() {
        Logging.d(TAG, "leaveChannel: ");
        mEngine.leaveChannel();
    }

    private void setClientRole(AliRtcEngine.AliRTCSDK_Client_Role aliRTCSDKClientRole) {
        mEngine.setClientRole(aliRTCSDKClientRole);
    }

    private void startPublish() {
        Logging.d(TAG, "startPublish: ");
        mEngine.configLocalAudioPublish(true);
        mEngine.configLocalCameraPublish(false);
        mEngine.configLocalScreenPublish(false);
        mEngine.publish();
    }

    private void stopPublish() {
        Logging.d(TAG, "stopPublish: ");
        mEngine.configLocalAudioPublish(false);
        mEngine.configLocalCameraPublish(false);
        mEngine.configLocalScreenPublish(false);
        mEngine.publish();
    }

    private void parseSeatListInfo(SeatListInfo seatListInfo) {
        if (seatListInfo == null || seatListInfo.getData() == null) {
            return;
        }
        synchronized (RTCAudioLiveRoomImpl.class) {
            ArrayList<SeatInfo> seatListInfos = seatListInfo.getData();
            //清除下麦用户
            removeLeaveSeatUser(seatListInfos);
            //添加新上麦用户
            addEnterSeatUser(seatListInfos);
        }

    }

    private void addEnterSeatUser(ArrayList<SeatInfo> seatListInfos) {

        for (final SeatInfo seatInfo : seatListInfos) {
            Log.i(TAG, "addEnterSeatUser: " + mSeatInfos);
            //数组角标
            int index = indexOfSeatList(seatInfo);
            //不存在或者麦序为-1时代表时先回调的静音再获取的麦序，需要把静音信息设置到新的麦序中
            if (index == -1 || getSeatIndex(mSeatInfos.get(index).getSeatIndex()) < 0) {
                if (index != -1) {
                    seatInfo.setMuteMic(mSeatInfos.get(index).isMuteMic());
                }
                if (mRTCAudioLiveRoomDelegate != null) {
                    if (StringUtils.equals(seatInfo.getUserId(), mLocalUserId)) {
                        //自己
                        seatInfo.setUserName(mUserName);
                    } else {
                        //远端用户
                        AliRtcRemoteUserInfo userInfo = getUserInfo(seatInfo.getUserId());
                        if (userInfo == null) {
                            continue;
                        }
                        seatInfo.setUserName(userInfo.getDisplayName());
                    }
                    updateSeatInfo(seatInfo);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRTCAudioLiveRoomDelegate.onEnterSeat(seatInfo);
                        }
                    });
                }
            }
        }
    }

    private void removeLeaveSeatUser(ArrayList<SeatInfo> seatListInfos) {
        for (int i = mSeatInfos.size() - 1; i >= 0; i--) {
            final SeatInfo seatInfo = mSeatInfos.get(i);
            int index = seatListInfos.indexOf(seatInfo);
            if (index == -1) {
                if (mRTCAudioLiveRoomDelegate != null) {
                    removeSeatInfo(seatInfo);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRTCAudioLiveRoomDelegate.onLeaveSeat(seatInfo);
                        }
                    });
                }
            }
        }
    }

    private AliRtcRemoteUserInfo getUserInfo(String userId) {
        return mEngine.getUserInfo(userId);
    }

    private void joinChannel(String channelId, final String userName) {
        mRtcAuthInfoLoader.loadRTCAuthInfo(channelId, new OkhttpClient.BaseHttpCallBack<AudioRoomUserInfoResponse>() {

            @Override
            public void onSuccess(AudioRoomUserInfoResponse data) {
                if (data != null && data.getData() != null) {
                    AudioRoomUserInfoResponse.RtcAuthInfo rtcAuthInfo = data.getData();
                    AliRtcAuthInfo aliRtcAuthInfo = createAliRtcAuthInfo(rtcAuthInfo);
                    mLocalUserId = aliRtcAuthInfo.getUserId();
                    mEngine.joinChannel(aliRtcAuthInfo, userName);
                } else if (mRTCAudioLiveRoomDelegate != null){
                    joinChannelResult(JOIN_CHANNEL_FAILD_CODE_BY_BAD_NETWORK);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Logging.d(TAG, errorMsg);
                joinChannelResult(JOIN_CHANNEL_FAILD_CODE_BY_BAD_NETWORK);
            }
        });
    }

    private void joinChannelResult(final int result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRTCAudioLiveRoomDelegate != null) {
                    mRTCAudioLiveRoomDelegate.onJoinChannelResult(result, mLocalUserId);
                }
            }
        });
    }

    private AliRtcAuthInfo createAliRtcAuthInfo(AudioRoomUserInfoResponse.RtcAuthInfo rtcAuthInfo) {
        if (rtcAuthInfo == null) {
            return null;
        }
        List<String> gslb = rtcAuthInfo.getGslb();
        AliRtcAuthInfo userInfo = new AliRtcAuthInfo();
        //频道ID
        userInfo.setConferenceId(rtcAuthInfo.getChannelId());
        String appid = rtcAuthInfo.getAppid();
        /* 应用ID */
        userInfo.setAppid(appid);
        /* 随机码 */
        userInfo.setNonce(rtcAuthInfo.getNonce());
        /* 时间戳*/
        userInfo.setTimestamp(rtcAuthInfo.getTimestamp());
        String userid = rtcAuthInfo.getUserid();
        /* 用户ID
         * */
        userInfo.setUserId(userid);
        /* GSLB地址*/
        userInfo.setGslb(gslb.toArray(new String[0]));
        /*鉴权令牌Token*/
        userInfo.setToken(rtcAuthInfo.getToken());
        return userInfo;
    }

    /**
     * 对数据操作进行加锁
     */
    private SeatInfo findSeatInfoById(String uid) {
        synchronized (mRtcUserInfoLock) {
            if (mSeatInfos == null) {
                return null;
            }
            SeatInfo seatInfo = new SeatInfo();
            seatInfo.setUserId(uid);
            int index = mSeatInfos.indexOf(seatInfo);
            if (index != -1) {
                return mSeatInfos.get(index);
            }
            return null;
        }
    }

    private void updateSeatInfoBySpeakState(String uid, boolean isSpeaking) {
        synchronized (mRtcUserInfoLock) {
            SeatInfo seatInfo = new SeatInfo();
            seatInfo.setUserId(uid);
            seatInfo.setSpeaking(isSpeaking);
            if (mSeatInfos == null) {
                return;
            }
            if (mSeatInfos.contains(seatInfo)) {
                mSeatInfos.set(mSeatInfos.indexOf(seatInfo), seatInfo);
            } else {
                mSeatInfos.add(seatInfo);
            }
        }
    }

    private void updateSeatInfo(SeatInfo seatInfo) {
        synchronized (mRtcUserInfoLock) {
            if (mSeatInfos == null) {
                return;
            }
            if (mSeatInfos.contains(seatInfo)) {
                mSeatInfos.set(mSeatInfos.indexOf(seatInfo), seatInfo);
            } else {
                mSeatInfos.add(seatInfo);
            }
        }
    }

    private void updateSeatInfoByMute(String uid, boolean mute) {
        synchronized (mRtcUserInfoLock) {
            SeatInfo seatInfo = new SeatInfo();
            seatInfo.setUserId(uid);
            seatInfo.setMuteMic(mute);
            if (mSeatInfos == null) {
                return;
            }
            if (mSeatInfos.contains(seatInfo)) {
                mSeatInfos.set(mSeatInfos.indexOf(seatInfo), seatInfo);
            } else {
                mSeatInfos.add(seatInfo);
            }
        }
    }

    private void removeSeatInfo(SeatInfo seatInfo) {
        synchronized (mRtcUserInfoLock) {
            if (mSeatInfos != null) {
                mSeatInfos.remove(seatInfo);
            }
        }
    }

    private int indexOfSeatList(SeatInfo seatInfo) {
        synchronized (mRtcUserInfoLock) {
            if (mSeatInfos != null) {
                return mSeatInfos.indexOf(seatInfo);
            }
            return -1;
        }
    }

}
