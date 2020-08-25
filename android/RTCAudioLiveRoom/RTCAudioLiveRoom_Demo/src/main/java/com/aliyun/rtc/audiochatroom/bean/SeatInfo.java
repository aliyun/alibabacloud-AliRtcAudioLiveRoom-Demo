package com.aliyun.rtc.audiochatroom.bean;

import android.support.annotation.Nullable;

import com.alivc.rtc.device.utils.StringUtils;

public class SeatInfo {

    private String seatIndex;
    private String userId;
    private String userName;
    private boolean muteMic;
    private boolean speaking;

    public String getSeatIndex() {
        return seatIndex;
    }

    public void setSeatIndex(String seatIndex) {
        this.seatIndex = seatIndex;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isMuteMic() {
        return muteMic;
    }

    public void setMuteMic(boolean muteMic) {
        this.muteMic = muteMic;
    }

    public boolean isSpeaking() {
        return speaking;
    }

    public void setSpeaking(boolean speaking) {
        this.speaking = speaking;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof SeatInfo && StringUtils.equals(((SeatInfo) obj).userId, this.userId);
    }
}
