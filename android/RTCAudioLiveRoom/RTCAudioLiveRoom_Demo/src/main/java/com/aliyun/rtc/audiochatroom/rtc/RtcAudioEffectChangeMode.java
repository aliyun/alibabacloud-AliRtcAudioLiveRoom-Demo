package com.aliyun.rtc.audiochatroom.rtc;

import com.alivc.rtc.AliRtcEngine;

public enum RtcAudioEffectChangeMode {
    //关闭
    ALIRTCSDK_AUDIOEFFECT_VOICE_CHANGER_OFF(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_OFF,"关闭"),
    //老人
    ALIRTCSDK_AUDIOEFFECT_VOICE_CHANGER_OLDMAN(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Oldman,"老人"),
    //男孩
    ALIRTCSDK_AUDIOEFFECT_VOICE_CHANGER_BABYBOY(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Babyboy,"男孩"),
    //女孩
    ALIRTCSDK_AUDIOEFFECT_VOICE_CHANGER_BABYGIRL(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Babygirl, "女孩"),
    //机器人
    ALIRTCSDK_AUDIOEFFECT_VOICE_CHANGER_ROBOT(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Robot,"机器人"),
    //大魔王
    ALIRTCSDK_AUDIOEFFECT_VOICE_CHANGER_DAIMO(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Daimo,"大魔王"),
    //KTV
    ALIRTCSDK_AUDIOEFFECT_VOICE_CHANGER_KTV(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Ktv,"KTV"),
    //回声
    ALIRTCSDK_AUDIOEFFECT_VOICE_CHANGER_ECHO(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Echo,"回声");
    private AliRtcEngine.AliRtcAudioEffectVoiceChangerMode mode;
    private String des;

    RtcAudioEffectChangeMode(AliRtcEngine.AliRtcAudioEffectVoiceChangerMode mode, String des) {
        this.mode = mode;
        this.des = des;
    }

    public AliRtcEngine.AliRtcAudioEffectVoiceChangerMode getMode() {
        return mode;
    }

    public String getDes() {
        return des;
    }
}
