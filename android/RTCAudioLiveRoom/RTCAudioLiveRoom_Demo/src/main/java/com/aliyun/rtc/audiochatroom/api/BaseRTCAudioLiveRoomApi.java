package com.aliyun.rtc.audiochatroom.api;


import com.aliyun.rtc.audiochatroom.api.net.OkhttpClient;

public abstract class BaseRTCAudioLiveRoomApi {

    public abstract <T> void joinChannelSuccess(String channelId, String uid, String userName, OkhttpClient.BaseHttpCallBack<T> callBack);
    public abstract <T> void describeChannelUsers(String channelId, OkhttpClient.BaseHttpCallBack<T> callBack);
}
