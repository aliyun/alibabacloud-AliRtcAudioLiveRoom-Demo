package com.aliyun.rtc.audiochatroom.api.impl;

import com.aliyun.rtc.audiochatroom.api.AudioLiveRoomModelFactory;
import com.aliyun.rtc.audiochatroom.api.BaseRTCAuthInfoLoader;
import com.aliyun.rtc.audiochatroom.api.BaseSeatInfoLoader;

public class AudioLiveRoomModelFactoryImpl implements AudioLiveRoomModelFactory {

    public static <T> T createLoader(Class<T> tClass) {
        T t = null;
        try {
            t = tClass.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return t;
    }

    @Override
    public BaseSeatInfoLoader createSeatInfoLoader() {
        return  createLoader(SeatInfoLoaderImpl.class);
    }

    @Override
    public BaseRTCAuthInfoLoader createRTCAuthInfoLoader() {
        return createLoader(RTCAuthInfoLoaderImpl.class);
    }
}
