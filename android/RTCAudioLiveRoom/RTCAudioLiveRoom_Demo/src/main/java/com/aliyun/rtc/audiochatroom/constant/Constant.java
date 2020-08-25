package com.aliyun.rtc.audiochatroom.constant;

public class Constant {

    public static final String NEW_TOKEN_PARAMS_KEY_USERID = "userid";
    public static final String PATH_ASSETS_BGM = "mp3/bgm.zip";
    public static final String PATH_ASSETS_AUDIOEFFECT = "mp3/audioeffect.zip";
    public static final String PATH_DIR_BGM_OUT = "bgm";
    public static final String PATH_DIR_AUDIOEFFECT_OUT = "audioeffect";
    //背景乐、音效默认音量
    public static final int VALUE_AUDIO_EFFECT_VOLUME = 100;
    public static final String NEW_TOKEN_PARAMS_KEY_USERNAME = "userName";
    /**
     * server端的请求域名，需要用户自己替换成自己server端的域名
     */
    private static final String BASE_URL = "";
    /**
     * 获取鉴权信息
     */
    private static final String URL_RANDOM_USER = BASE_URL + "/user/randomUser";
    //加入房间成功
    private static final String URL_JOIN_CHANNEL_SUCCESS = BASE_URL + "/user/joinSuccess";
    //获取麦序
    private static final String URL_GET_SEAT_LIST = BASE_URL + "/user/getSeatList";

    private static final String URL_GET_CHANNEL_USERS = BASE_URL + "/user/describeChannelUsers";

    public static final String NEW_TOKEN_PARAMS_KEY_CHANNELID = "channelId";
    //最大麦序数量
    public static final int MAX_SEAT_COUNT = 8;

    public static String getRandomUserUrl() {
        return URL_RANDOM_USER;
    }

    public static String getJoinChannelSuccessUrl() {
        return URL_JOIN_CHANNEL_SUCCESS;
    }

    public static String getSeatListUrl() {
        return URL_GET_SEAT_LIST;
    }

    public static String getChannelUsersUrl() {
        return URL_GET_CHANNEL_USERS;
    }
}
