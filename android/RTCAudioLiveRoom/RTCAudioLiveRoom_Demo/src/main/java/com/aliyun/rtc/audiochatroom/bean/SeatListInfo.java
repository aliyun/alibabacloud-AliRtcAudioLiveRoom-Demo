package com.aliyun.rtc.audiochatroom.bean;

import java.util.ArrayList;

public class SeatListInfo {

    private String result;
    private String requestId;
    private String message;
    private String code;
    private ArrayList<SeatInfo> data;

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

    public ArrayList<SeatInfo> getData() {
        return data;
    }

    public void setData(ArrayList<SeatInfo> data) {
        this.data = data;
    }

}
