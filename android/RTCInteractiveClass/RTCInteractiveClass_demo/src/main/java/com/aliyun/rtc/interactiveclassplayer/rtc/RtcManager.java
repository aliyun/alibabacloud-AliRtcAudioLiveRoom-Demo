package com.aliyun.rtc.interactiveclassplayer.rtc;
import android.text.TextUtils;
import android.util.Log;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcEngineEventListener;
import com.alivc.rtc.AliRtcEngineNotify;
import com.alivc.rtc.AliRtcRemoteUserInfo;
import com.aliyun.rtc.interactiveclassplayer.R;
import com.aliyun.rtc.interactiveclassplayer.utils.ApplicationContextUtil;
import com.aliyun.rtc.interactiveclassplayer.utils.ToastUtils;
import com.aliyun.rtc.interactiveclassplayer.utils.UIHandlerUtil;

import org.webrtc.alirtcInterface.AliParticipantInfo;
import org.webrtc.alirtcInterface.AliStatusInfo;
import org.webrtc.alirtcInterface.AliSubscriberInfo;

import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_SDK_INVALID_STATE;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_SESSION_REMOVED;

public class RtcManager {
    public AliRtcEngine mEngine;
    private static boolean hasShowBadNetwork;

    private static final String TAG = RtcManager.class.getSimpleName();

    private RtcManager() {
    }

    public AliRtcRemoteUserInfo getUserInfo(String s) {
        if (mEngine != null) {
            return mEngine.getUserInfo(s);
        }
        return null;
    }

    private static final class RtcManagerInstance {
        private static final RtcManager INSTANCE = new RtcManager();
    }

    public static RtcManager getInstance() {
        return RtcManagerInstance.INSTANCE;
    }

    public void init() {
        if (mEngine == null) {
            //对h5页面的支持
            AliRtcEngine.setH5CompatibleMode(1);
            mEngine = AliRtcEngine.getInstance(ApplicationContextUtil.getAppContext());
            //设置视频固定横屏显示
            mEngine.setDeviceOrientationMode(AliRtcEngine.AliRtcOrientationMode.AliRtcOrientationModeLandscapeLeft);
            //默认横屏时交换视频宽高
            mEngine.setVideoSwapWidthAndHeight(true, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
            //默认开启扬声器
            mEngine.enableSpeakerphone(true);
            mEngine.enableHighDefinitionPreview(false);
            //自动发布、手动订阅
            mEngine.setAutoPublishSubscribe(true, false);
        }
    }

    /**
     * 加入房间
     *
     * @param aliRtcAuthInfo 用户信息
     * @param displayName     名称
     */
    public void joinChannel(AliRtcAuthInfo aliRtcAuthInfo, String displayName) {
        if (mEngine != null) {
            mEngine.joinChannel(aliRtcAuthInfo, displayName);
        }
    }

    /**
     * 设置是否自动发布本地流和订阅远端流
     *
     * @param autoPublish  自动发布
     * @param autoSubscribe 自动订阅
     */
    public void setAutoPublishSubscribe(boolean autoPublish, boolean autoSubscribe) {
        if (mEngine != null) {
            mEngine.setAutoPublishSubscribe(false, true);
        }
    }


    public void leaveAndDestroy() {
        if (mEngine != null) {
            mEngine.leaveChannel();
            mEngine.destroy();
            mEngine = null;
        }
    }
    public void setRtcEngineEventListener(AliRtcEngineEventListener listener) {
        if (mEngine != null) {
            mEngine.setRtcEngineEventListener(listener);
        }
    }

    public static class SimpleRtcEngineEventListener extends AliRtcEngineEventListener {

        @Override
        public void onJoinChannelResult(int result) {
            Log.i(TAG, "onJoinChannelResult: result --> " + result);
        }

        public void onLiveStreamingSignalingResult(int i) {
            Log.i(TAG, "onLiveStreamingSignalingResult: " + i);
        }
        /**
         * 离开房间的回调
         *
         * @param i 结果码
         */
        @Override
        public void onLeaveChannelResult(int i) {
            Log.i(TAG, "onLeaveChannelResult: i --> " + i);
        }

        /**
         * 网络状态变化的回调
         *
         * @param aliRtcNetworkQuality1 下行网络质量
         * @param aliRtcNetworkQuality  上行网络质量
         * @param s                     String  用户ID
         */
        @Override
        public void onNetworkQualityChanged(final String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1) {
            synchronized (RtcManager.class) {
                if (aliRtcNetworkQuality1.getValue() >= AliRtcEngine.AliRtcNetworkQuality.Network_Bad.getValue() && aliRtcNetworkQuality1.getValue() <= AliRtcEngine.AliRtcNetworkQuality.Network_VeryBad.getValue() && !hasShowBadNetwork) {//网络质量差
                    hasShowBadNetwork = true;
                    if (TextUtils.isEmpty(s)) {
                        showToast(R.string.alivc_biginteractiveclass_string_network_bad);
                    } else {
                        showToast(R.string.alivc_biginteractiveclass_string_remote_user_network_bad);
                    }
                } else if (TextUtils.isEmpty(s) && aliRtcNetworkQuality1.getValue() <= AliRtcEngine.AliRtcNetworkQuality.Network_Good.getValue()) {
                    hasShowBadNetwork = false;
                }
            }
        }

        /**
         * 出现警告的回调
         *
         * @param i 错误码
         */
        @Override
        public void onOccurWarning(int i) {
            Log.i(TAG, "onOccurWarning: i --> " + i);
        }

        /**
         * 出现错误的回调
         *
         * @param error 错误码
         */
        @Override
        public void onOccurError(int error) {
            Log.i(TAG, "onOccurError: error --> " + error);
            //出现这几个错误码需要销毁sdk，否则无法再次观看
            switch (error) {
            case ERR_SDK_INVALID_STATE:
            case ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT:
            case ERR_SESSION_REMOVED:
                //先销毁在创建
                getInstance().destroy();
                break;
            //local network disconnect
            case 259:
                showToast(R.string.alivc_biginteractiveclass_string_rtc_network_conn_error);
                break;
            default:
                break;
            }
        }

        /**
         * 当前设备性能不足
         */
        @Override
        public void onPerformanceLow() {
            Log.i(TAG, "onPerformanceLow: ");
        }

        /**
         * 当前设备性能恢复
         */
        @Override
        public void onPermormanceRecovery() {
            Log.i(TAG, "onPermormanceRecovery: ");

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

        public void onUpdateRoleNotify(AliRtcEngine.AliRTCSDK_Client_Role oldRole, AliRtcEngine.AliRTCSDK_Client_Role newRole) {}

        /**
         * 推流结果回调
         * @param result
         * @param isPublished
         */
        public void onPublishChangedNotify(int result, boolean isPublished) {
            //推流失败
            if (result != 0) {
                showToast(R.string.alivc_biginteractiveclass_string_publish_error);
            }
        }

    }

    public void setRtcEngineNotify(AliRtcEngineNotify listener) {
        if (mEngine != null) {
            mEngine.setRtcEngineNotify(listener);
        }
    }

    /**
     * SDK事件通知(回调接口都在子线程)
     */
    public static class SimpleAliRtcEngineNotify extends AliRtcEngineNotify {

        /**
         * 远端用户上线通知
         *
         * @param s userid
         */
        @Override
        public void onRemoteUserOnLineNotify(String s) {
            Log.i(TAG, "onRemoteUserOnLineNotify: s --> " + s);
        }

        /**
         * 远端用户下线通知
         *
         * @param s userid
         */
        @Override
        public void onRemoteUserOffLineNotify(String s) {
            Log.i(TAG, "onRemoteUserOffLineNotify: s --> " + s);
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
            Log.i(TAG, "onRemoteTrackAvailableNotify: s --> " + s + ", aliRtcVideoTrack --> " + aliRtcVideoTrack);
        }

        /**
         * 订阅流回调，可以做UI及数据的更新
         *
         * @param s                userid
         * @param aliRtcAudioTrack 音频流
         * @param aliRtcVideoTrack 相机流
         */
        @Override
        public void onSubscribeChangedNotify(String s, AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack,
                                             AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
            Log.i(TAG, "onSubscribeChangedNotify: s --> " + s + ", aliRtcVideoTrack --> " + aliRtcVideoTrack);
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
            Log.i(TAG, "onFirstFramereceived: ");
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
            Log.i(TAG, "onFirstPacketSent: ");
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
            Log.i(TAG, "onFirstPacketReceived: ");
        }

        /**
         * 被服务器踢出或者频道关闭时回调
         *
         */
        @Override
        public void onBye(int i) {
            Log.i(TAG, "onBye: ");
        }

        @Override
        public void onParticipantStatusNotify(AliStatusInfo[] aliStatusInfos, int i) {
            Log.i(TAG, "onParticipantStatusNotify: ");
        }

    }

    /**
     * 设置播放远端视频流
     */
    public void setRemoteViewConfig(AliRtcEngine.AliVideoCanvas aliVideoCanvas, String uid, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
        Log.i(TAG, "setRemoteViewConfig: ");
        if (mEngine != null) {
            mEngine.setRemoteViewConfig(aliVideoCanvas, uid, aliRtcVideoTrack);
        }
    }

    /**
     * 销毁当前实例
     */
    public void destroy() {
        if (mEngine != null) {
            mEngine.destroy();
            mEngine = null;
        }
    }

    /**
     * 是否发布本地音频流
     * @return 返回0为切换成功，其他为切换失败
     */
    public int muteLocalMic(boolean muteLocalMic) {
        if (mEngine != null) {
            return mEngine.muteLocalMic(muteLocalMic);
        }
        return -1;
    }

    /**
     * 是否发布本地相机流
     * @return 返回0为切换成功，其他为切换失败
     */
    public int muteLocalCamera(boolean muteLocalCamera, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
        if (mEngine != null) {
            return mEngine.muteLocalCamera(muteLocalCamera, aliRtcVideoTrack);
        }
        return -1;
    }

    /**
     * 切换前后摄像头
     * @return 返回0为切换成功，其他为切换失败
     */
    public int switchCamera() {
        if (mEngine != null) {
            return mEngine.switchCamera();
        }
        return -1;
    }

    /**
     * 播放本地视频流
     * @param localAliVideoCanvas canvas
     * @param aliRtcVideoTrackCamera 类型
     */
    public void setLocalViewConfig(AliRtcEngine.AliVideoCanvas localAliVideoCanvas, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrackCamera) {
        Log.i(TAG, "setLocalViewConfig: ");
        if (mEngine != null) {
            mEngine.setLocalViewConfig(localAliVideoCanvas, aliRtcVideoTrackCamera);
        }
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        if (mEngine != null) {
            mEngine.startPreview();
        }
    }


    public void stopPreview() {
        if (mEngine != null) {
            mEngine.stopPreview();
        }
    }

    public void setDeviceOrientationMode(AliRtcEngine.AliRtcOrientationMode aliRtcOrientationMode) {
        if (mEngine != null) {
            mEngine.setDeviceOrientationMode(aliRtcOrientationMode);
        }
    }

    public void startPublish() {
        if (mEngine != null) {
            mEngine.configLocalCameraPublish(true);
            mEngine.configLocalAudioPublish(true);
            //移动端不涉及屏幕分享
//            mEngine.configLocalScreenPublish(true);
            mEngine.publish();
        }
    }

    public void stopPublish() {
        if (mEngine != null) {
            mEngine.configLocalCameraPublish(false);
            mEngine.configLocalAudioPublish(false);
            //移动端不涉及屏幕分享
//            mEngine.configLocalScreenPublish(false);
            mEngine.publish();
        }
    }

    /**
     * 设置是否订阅远端相机流。默认为订阅大流，手动订阅时，需要调用subscribe才能生效。
     * @param userId userid
     * @param master true为优先订阅大流，false为订阅次小流。
     * @param enable true为订阅远端相机流，false为停止订阅远端相机流。
     */
    public void configRemoteCameraTrack(String userId, boolean master, boolean enable) {
        if (mEngine != null) {
            mEngine.configRemoteCameraTrack(userId, master, enable);
            // 订阅远端音频流。
            mEngine.configRemoteAudio(userId, true);
            // 订阅远端屏幕流。
            mEngine.configRemoteScreenTrack(userId, true);
            mEngine.subscribe(userId);
        }
    }

    private static void showToast(final String s) {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                ToastUtils.showInCenter(ApplicationContextUtil.getAppContext(), s);
            }
        });
    }

    private static void showToast(final int resId) {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                String string = ApplicationContextUtil.getAppContext().getString(resId);
                ToastUtils.showInCenter(ApplicationContextUtil.getAppContext(), string);
            }
        });
    }
}
