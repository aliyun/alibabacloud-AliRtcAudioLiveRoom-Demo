package com.aliyun.rtc.audiochatroom.rtc;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.rtc.audiochatroom.bean.SeatInfo;

public interface RTCAudioLiveRoomDelegate {
    /**
     * 登陆回调
     *
     * @param result 状态码
     * @param uid 自己的uid
     */
    void onJoinChannelResult(int result, String uid);

    /**
     * 上麦通知回调
     *
     * @param seatInfo 麦位信息
     */
    void onEnterSeat(SeatInfo seatInfo);

    /**
     * 下麦通知回调
     *
     * @param seatInfo 麦位信息
     */
    void onLeaveSeat(SeatInfo seatInfo);

    /**
     * 退出房间回调
     * @param result 退出房间回调 0表示成功 反之失败
     */
    void onLeaveChannelResult(int result);

    /**
     * 用户音量更新回调
     * @param seatIndex 麦序
     * @param isSpeaking 是否正在说话
     */
    void onSeatVolumeChanged(int seatIndex, boolean isSpeaking);

    /**
     * 播放状态更新回调
     *
     * @param audioPlayingStatus 当前播放状态
     */
    void onAudioPlayingStateChanged(AliRtcEngine.AliRtcAudioPlayingStateCode audioPlayingStatus);

    /**
     * 用户静音回调
     *
     * @param seatIndex 麦序
     * @param mute      是否静音
     */
    void onSeatMutedChanged(int seatIndex, boolean mute);

    /**
     * 房间被销毁回调
     */
    void onRoomDestroy();

    /**
     * sdk报错,需要销毁实例
     * @param error 错误码
     */
    void onOccurError(int error);

    /**
     * 角色切换成功
     *
     * @param oldRole 旧的用户角色
     * @param newRole 新的用户角色
     */
    void onUpdateRoleNotify(AliRtcEngine.AliRTCSDK_Client_Role oldRole, AliRtcEngine.AliRTCSDK_Client_Role newRole);

    /**
     * 网络状态回调
     *
     * @param aliRtcNetworkQuality1 下行网络质量
     * @param aliRtcNetworkQuality  上行网络质量
     * @param s                     String  用户ID
     */
    void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1);

    /**
     * 推流结果回调
     * @param result 返回码 0表示推流成功，反之失败
     * @param isPublished 是否再推流
     */
    void onPublishChangedNotify(int result, boolean isPublished);
}
