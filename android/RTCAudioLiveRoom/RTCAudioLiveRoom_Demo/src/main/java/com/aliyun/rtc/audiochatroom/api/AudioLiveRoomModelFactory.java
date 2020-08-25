package com.aliyun.rtc.audiochatroom.api;

public interface AudioLiveRoomModelFactory {
    BaseSeatInfoLoader createSeatInfoLoader();

    BaseRTCAuthInfoLoader createRTCAuthInfoLoader();
}
