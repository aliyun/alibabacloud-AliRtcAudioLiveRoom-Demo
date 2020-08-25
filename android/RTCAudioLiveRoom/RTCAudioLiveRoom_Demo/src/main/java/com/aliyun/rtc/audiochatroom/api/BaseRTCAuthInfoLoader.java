package com.aliyun.rtc.audiochatroom.api;

import com.aliyun.rtc.audiochatroom.api.net.OkhttpClient;

public abstract class BaseRTCAuthInfoLoader {
    public abstract <T> void loadRTCAuthInfo(String channelId, OkhttpClient.BaseHttpCallBack<T> callBack);
}
