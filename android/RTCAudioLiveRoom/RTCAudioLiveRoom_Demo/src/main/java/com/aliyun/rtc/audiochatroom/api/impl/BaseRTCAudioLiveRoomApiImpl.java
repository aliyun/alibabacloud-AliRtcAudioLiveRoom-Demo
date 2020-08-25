package com.aliyun.rtc.audiochatroom.api.impl;


import com.aliyun.rtc.audiochatroom.api.BaseRTCAudioLiveRoomApi;
import com.aliyun.rtc.audiochatroom.api.net.OkHttpCientManager;
import com.aliyun.rtc.audiochatroom.api.net.OkhttpClient;
import com.aliyun.rtc.audiochatroom.constant.Constant;

import java.util.HashMap;
import java.util.Map;


public class BaseRTCAudioLiveRoomApiImpl extends BaseRTCAudioLiveRoomApi {

    @Override
    public <T> void joinChannelSuccess(String channelId, String uid, String userName, OkhttpClient.BaseHttpCallBack<T> callBack) {
        String url = Constant.getJoinChannelSuccessUrl();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId);
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_USERID, uid);
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_USERNAME, userName);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }


    @Override
    public <T> void describeChannelUsers(String channelId, OkhttpClient.BaseHttpCallBack<T> callBack) {
        String url = Constant.getChannelUsersUrl();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }

}
