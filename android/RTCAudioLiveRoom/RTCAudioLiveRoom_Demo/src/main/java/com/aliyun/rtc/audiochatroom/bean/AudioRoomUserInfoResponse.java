package com.aliyun.rtc.audiochatroom.bean;

import java.io.Serializable;
import java.util.List;

public class AudioRoomUserInfoResponse implements Serializable {

    private String result;
    private String requestId;
    private String message;
    private String code;
    private RtcAuthInfo data;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public RtcAuthInfo getData() {
        return data;
    }

    public void setData(RtcAuthInfo data) {
        this.data = data;
    }

    public static class RtcAuthInfo implements Serializable{
        private String appid;
        private String userName;
        private String userid;
        private String nonce;
        private String channelId;
        private String token;
        private int timestamp;
        private List<String> gslb;

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(int timestamp) {
            this.timestamp = timestamp;
        }

        public List<String> getGslb() {
            return gslb;
        }

        public void setGslb(List<String> gslb) {
            this.gslb = gslb;
        }
    }
}
