package com.aliyun.rtc.audiochatroom.rtc;

import com.alivc.rtc.AliRtcEngine;

public enum RtcAudioEffectReverbMode {
    //无效果
    AliRTCSDK_AudioEffect_Reverb_Off(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Off,"无效果"),
    //人声I
    AliRtcSdk_AudioEffect_Reverb_Vocal_I(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Vocal_I,"人声I"),
    //人声II
    AliRTCSDK_AudioEffect_Reverb_Vocal_II(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Vocal_II,"人声II"),
    //澡堂
    AliRTCSDK_AudioEffect_Reverb_Bathroom(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Bathroom, "澡堂"),
    //明亮的小房间
    AliRTCSDK_AudioEffect_Reverb_Small_Room_Bright(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Small_Room_Bright,"明亮的小房间"),
    //黑暗的小房间
    AliRTCSDK_AudioEffect_Reverb_Small_Room_Dark(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Small_Room_Dark,"黑暗的小房间"),
    //中等大小房间
    AliRTCSDK_AudioEffect_Reverb_Medium_Room(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Medium_Room,"中等大小房间"),
    //大房间
    AliRTCSDK_AudioEffect_Reverb_Large_Room(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Large_Room,"大房间"),
    //教堂走廊
    AliRTCSDK_AudioEffect_Reverb_Church_Hall(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Church_Hall,"教堂走廊"),
    //大教堂
    AliRTCSDK_AudioEffect_Reverb_Cathedral(AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Cathedral,"大教堂");

    private AliRtcEngine.AliRtcAudioEffectReverbMode mode;
    private String des;

    RtcAudioEffectReverbMode(AliRtcEngine.AliRtcAudioEffectReverbMode mode, String des) {
        this.mode = mode;
        this.des = des;
    }

    public AliRtcEngine.AliRtcAudioEffectReverbMode getMode() {
        return mode;
    }

    public String getDes() {
        return des;
    }
}
