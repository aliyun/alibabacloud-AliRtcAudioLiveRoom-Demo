package com.aliyun.rtc.voicecall.constant;

public class Constant {

    //asssets file path
    public static final String CACHE_PATH = "bgm";
    public static final String ASSETS_BGM_PATH = "mp3";
    //频道可加入的最大人数
    public static final int ALIVC_VOICE_CALL_MAX_CHANNEL_USER_NUM = 2;

    private static final String BASE_URL = "";
    /**
     * 生成新用户 get
     */
    private static final String URL_NEW_GUEST = BASE_URL + "/user/randomUser";
    private static final String URL_NEW_TOKEN = BASE_URL + "/app/token";
    private static final String URL_CHANNEL_START_TIME = BASE_URL + "/app/descChannelStartTime";
    private static final String URL_DES_CHANNEL_USERS = BASE_URL + "/app/descChannelUsers";

    public static final String NEW_GUEST_PARAMS_KEY_PACKAGE = "PACKAGE_NAME";
    public static final String NEW_TOKEN_PARAMS_KEY_CHANNELID = "channelId";
    //"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    public static final String UTC_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    //sp key
    public static final String ALIVC_VOICE_CALL_SP_KEY_USER_INFO = "user_info";

    public static String getUrlNewGuest() {
        return URL_NEW_GUEST;
    }

    public static String getUserLoginUrl() {
        return URL_NEW_TOKEN;
    }

    public static String getChannelUserInfo() {
        return URL_DES_CHANNEL_USERS;
    }

    public static String getChannelStartTime() {
        return URL_CHANNEL_START_TIME;
    }
}
