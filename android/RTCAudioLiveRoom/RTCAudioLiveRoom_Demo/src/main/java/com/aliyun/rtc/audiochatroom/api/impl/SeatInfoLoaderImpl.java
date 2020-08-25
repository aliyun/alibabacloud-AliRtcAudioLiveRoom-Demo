package com.aliyun.rtc.audiochatroom.api.impl;

import com.aliyun.rtc.audiochatroom.api.BaseSeatInfoLoader;
import com.aliyun.rtc.audiochatroom.api.net.OkHttpCientManager;
import com.aliyun.rtc.audiochatroom.api.net.OkhttpClient;
import com.aliyun.rtc.audiochatroom.constant.Constant;

import java.util.HashMap;
import java.util.Map;

public class SeatInfoLoaderImpl extends BaseSeatInfoLoader {
    /**
     * 获取麦序信息
     * @param channelId 房间号
     * @param callBack 回调
     * @param <T> 返回的对象泛型
     */
    @Override
    public <T> void getSeatList(String channelId, OkhttpClient.BaseHttpCallBack<T> callBack) {
        String url = Constant.getSeatListUrl();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }
}
