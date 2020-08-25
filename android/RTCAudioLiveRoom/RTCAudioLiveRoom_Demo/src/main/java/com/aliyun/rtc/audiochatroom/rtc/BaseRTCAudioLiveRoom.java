package com.aliyun.rtc.audiochatroom.rtc;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;

public abstract class BaseRTCAudioLiveRoom {
    /**
     * 获取单例
     */
    public static BaseRTCAudioLiveRoom sharedInstance() {
        return RTCAudioLiveRoomImpl.sharedInstance();
    }

    /**
     * 加入房间
     *
     * @param role           角色类型
     * @param channelId      房间号
     * @param userName       用户名
     */
    public abstract void login(String channelId, String userName, AliRtcEngine.AliRTCSDK_Client_Role role);

    /**
     * 退出房间
     */
    public abstract void logout();

    /**
     * 设置音效音量
     *
     * @param soundId 音效文件的sourceId
     * @param volume 音量 区间0-100
     *
     */
    public abstract void setAudioEffectVolume(int soundId, int volume);

    /**
     * 设置伴奏音量
     * @param volume 音量 区间0-100
     */
    public abstract void setAudioAccompanyVolume(int volume);

    /**
     * 上麦
     */
    public abstract void enterSeat();

    /**
     * 下麦
     */
    public abstract void leaveSeat();

    /**
     * 是否开启静音模式
     *
     * @param mute true为静音 false不静音
     * @return 0表示设置成功，反之失败
     */
    public abstract int muteLocalMic(boolean mute);

    /**
     * 停止远端的所有音频流的播放。返回0为成功，其他返回错误码。
     *
     * @param enableSpeakerPhone true为开启 false关闭
     * @return 0表示设置成功，反之失败
     */
    public abstract int muteAllRemoteAudioPlaying(boolean enableSpeakerPhone);

    /**
     * 是否耳返
     *
     * @param enableEarBack 是否开启耳返 true为开启 false关闭
     * @return 0表示设置成功，反之失败
     */
    public abstract int enableEarBack(boolean enableEarBack);

    /**
     * 开始伴奏
     *
     * @param fileName      伴奏文件路径，支持本地文件和网络url
     * @param onlyLocalPlay 是否仅本地播放，true表示仅仅本地播放，false表示本地播放且推流到远端
     * @param replaceMic    是否替换mic的音频流，true表示伴奏音频流替换本地mic音频流，false表示伴奏音频流和mic音频流同时推
     * @param loopCycles    循环播放次数，-1表示一直循环
     */
    public abstract void startAudioAccompany(String fileName, boolean onlyLocalPlay, boolean replaceMic, int loopCycles);

    /**
     * 停止伴奏
     */
    public abstract void stopAudioAccompany();

    /**
     * 销毁实例
     */
    public abstract void destorySharedInstance();

    /**
     * 播放音效
     *
     * @param soundId  音效ID
     * @param filePath 音效文件路径，支持本地文件和网络url
     * @param cycles   循环播放次数。-1表示一直循环
     * @param publish  是否将音效音频流推到远端
     */
    public abstract void playAudioEffect(int soundId, String filePath, int cycles, boolean publish);

    /**
     * 停止音效
     *
     * @param soundId 音效ID
     */
    public abstract void stopAudioEffect(int soundId);

    /**
     * 设置音效场景
     * @param aliRtcAudioEffectReverbMode 音效场景枚举
     */
    public abstract void setAudioEffectReverbMode(AliRtcEngine.AliRtcAudioEffectReverbMode aliRtcAudioEffectReverbMode);

    /**
     * 设置监听
     *
     * @param audioLiveRoomDelegate 监听
     */
    public abstract void setRTCAudioLiveRoomDelegate(RTCAudioLiveRoomDelegate audioLiveRoomDelegate);

    /**
     * 设置变声音效模式
     *
     * @param mode 模式
     * @return int 结果码 0为成功
     */
    public abstract int setAudioEffectVoiceChangerMode(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode mode);

}
