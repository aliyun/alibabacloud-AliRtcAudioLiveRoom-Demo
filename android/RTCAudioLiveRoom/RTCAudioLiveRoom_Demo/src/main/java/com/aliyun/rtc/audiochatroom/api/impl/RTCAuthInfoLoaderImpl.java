package com.aliyun.rtc.audiochatroom.api.impl;

import com.aliyun.rtc.audiochatroom.api.BaseRTCAuthInfoLoader;
import com.aliyun.rtc.audiochatroom.api.net.OkHttpCientManager;
import com.aliyun.rtc.audiochatroom.api.net.OkhttpClient;
import com.aliyun.rtc.audiochatroom.constant.Constant;

import java.util.HashMap;
import java.util.Map;

public class RTCAuthInfoLoaderImpl extends BaseRTCAuthInfoLoader {

    @Override
    public <T> void loadRTCAuthInfo(String channelId, OkhttpClient.BaseHttpCallBack<T> callBack) {
        String url = Constant.getRandomUserUrl();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }
}
