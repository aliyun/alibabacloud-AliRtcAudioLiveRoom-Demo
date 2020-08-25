package com.aliyun.rtc.audiochatroom.rtc;

import android.util.Log;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.rtc.audiochatroom.bean.SeatInfo;

public class SimpleRTCAudioLiveRoomDelegate implements RTCAudioLiveRoomDelegate {

    private static final String TAG = RTCAudioLiveRoomDelegate.class.getSimpleName();

    @Override
    public void onJoinChannelResult(int result, String uid) {
        Log.i(TAG, "onJoinChannelResult: " + result);
    }

    @Override
    public void onEnterSeat(SeatInfo seatInfo) {
        Log.i(TAG, "onEnterSeat: " + seatInfo);
    }

    @Override
    public void onLeaveSeat(SeatInfo seatInfo) {
        Log.i(TAG, "onLeaveSeat: " + seatInfo);
    }

    @Override
    public void onLeaveChannelResult(int result) {
        Log.i(TAG, "onLeaveChannelResult: ");
    }

    @Override
    public void onSeatVolumeChanged(int seatIndex, boolean isSpeaking) {
        //        Log.i(TAG, "onSeatVolumeChanged: seatIndex : " + seatIndex + "; isSpeaking : " + isSpeaking);
    }

    @Override
    public void onAudioPlayingStateChanged(AliRtcEngine.AliRtcAudioPlayingStateCode audioPlayingStatus) {
        Log.i(TAG, "onAudioPlayingStateChanged: ");
    }

    @Override
    public void onSeatMutedChanged(int seatIndex, boolean mute) {
        Log.i(TAG, "onSeatMutedChanged: seatIndex : " + seatIndex + "; mute : " + mute);
    }


    @Override
    public void onRoomDestroy() {
        Log.i(TAG, "onRoomDestroy: ");
    }

    @Override
    public void onOccurError(int error) {
        Log.i(TAG, "onSDKError: ");
    }

    @Override
    public void onUpdateRoleNotify(AliRtcEngine.AliRTCSDK_Client_Role oldRole, AliRtcEngine.AliRTCSDK_Client_Role newRole) {

    }

    @Override
    public void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1) {

    }

    @Override
    public void onPublishChangedNotify(int result, boolean isPublished) {

    }
}
