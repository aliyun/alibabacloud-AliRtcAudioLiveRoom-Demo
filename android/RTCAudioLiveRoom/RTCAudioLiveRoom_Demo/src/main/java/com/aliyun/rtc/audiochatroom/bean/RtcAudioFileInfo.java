package com.aliyun.rtc.audiochatroom.bean;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.alivc.rtc.device.utils.StringUtils;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RtcAudioFileInfo {
    /**
     * 准备状态，可以开始播放
     */
    public static final int PERPARE = 0;
    /**
     * 正在播放并推流
     */
    public static final int PLAYING = 1;

    public static final int PAUSE = 2;

    public static final int RESUME = 3;

    public static final int STOP = 4;

    @IntDef({
            PERPARE, PLAYING, PAUSE, RESUME, STOP
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlayState {}

    public int playState, prePlayState;
    public int volume;
    public File file;

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof RtcAudioFileInfo && StringUtils.equals(((RtcAudioFileInfo) obj).file.getAbsolutePath(), this.file.getAbsolutePath());
    }
}
