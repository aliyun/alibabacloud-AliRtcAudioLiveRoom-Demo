package com.aliyun.rtc.voicecall.bean;

public class ChannelStartTimeResponse {

    private String result;
    private String requestId;
    private String message;
    private String code;
    private DataBean data;

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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {

        private long channelStartTimeTs;
        private String channelStartTimeUtc;

        public long getChannelStartTimeTs() {
            return channelStartTimeTs;
        }

        public void setChannelStartTimeTs(long channelStartTimeTs) {
            this.channelStartTimeTs = channelStartTimeTs;
        }

        public String getChannelStartTimeUtc() {
            return channelStartTimeUtc;
        }

        public void setChannelStartTimeUtc(String channelStartTimeUtc) {
            this.channelStartTimeUtc = channelStartTimeUtc;
        }
    }
}
