package com.aliyun.rtc.interactiveclassplayer.play;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.nativeclass.TrackInfo;

public interface LivePlayListener {
    //播放完成事件
    void onCompletion();

    //出错事件
    void loadingTimeOut();

    //准备成功事件
    void onPrepared();

    //视频分辨率变化回调
    void onVideoSizeChanged(int i, int i1);

    //首帧渲染显示事件
    void onRenderingStart();

    //缓冲开始
    void onLoadingBegin();

    //缓冲进度
    void onLoadingProgress(int i, float v);

    //缓冲结束
    void onLoadingEnd();

    //音视频清晰度切换成功
    void onChangedSuccess(TrackInfo trackInfo);

    //音视频清晰度切换失败
    void onChangedFail(TrackInfo trackInfo, ErrorInfo errorInfo);

    //播放器状态改变事件
    void onStateChanged(int i);
}
