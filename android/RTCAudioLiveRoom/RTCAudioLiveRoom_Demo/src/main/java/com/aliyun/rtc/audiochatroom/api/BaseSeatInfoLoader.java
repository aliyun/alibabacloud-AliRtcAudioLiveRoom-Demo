package com.aliyun.rtc.audiochatroom.api;

import com.aliyun.rtc.audiochatroom.api.net.OkhttpClient;

public abstract class BaseSeatInfoLoader {
    public abstract <T> void getSeatList(String channelId, OkhttpClient.BaseHttpCallBack<T> callBack);
}
