//
//  RTCAudioliveRoomManager.h
//  LectureHall
//
//  Created by Aliyun on 2020/6/15.
//  Copyright © 2020 alibaba. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AliRTCSdk/AliRTCSdk.h>
#import "SeatInfo.h"

NS_ASSUME_NONNULL_BEGIN

@protocol RTCAudioliveRoomDelegate <AliRtcEngineDelegate>

/// 远端用户上麦通知
/// @param seat 麦序
- (void)onEnterSeat:(SeatInfo *)seat;

/// 远端用户下线通知
/// @param seat 麦序
- (void)onLeaveSeat:(SeatInfo *)seat;

/// 房间被销毁通知
- (void)onRoomDestory;

/// 音量变化通知
/// @param seatIndex 麦序
/// @param isSpeaking 是否在说话
- (void)onSeatVolumeChanged:(NSInteger)seatIndex isSpeaking:(BOOL)isSpeaking;

/// 静音/取消静音变化通知
/// @param seatIndex 麦序
/// @param mute 是否静音
- (void)onSeatMutedChanged:(NSInteger)seatIndex mute:(BOOL)mute;

@end

@interface RTCAudioliveRoom : NSObject

@property (nonatomic, weak) id<RTCAudioliveRoomDelegate> delegate;

/// @brief 获取单例
/// @return RTCAudioliveRoomManager 单例对象
+ (RTCAudioliveRoom *) sharedInstance;

/// 销毁RTCSDK
-(void)destroySharedInstance;

/// 加入频道
/// @param channelId   频道名称
/// @param name    任意用于显示的用户名称。不是User ID
/// @param role   角色
/// @param handler   回调
- (void)login:(NSString *)channelId
         name:(NSString *)name
         role:(AliRtcClientRole)role
     complete:(void(^)(AliRtcAuthInfo *authInfo, NSInteger errorCode))handler;

/// 离开房间
- (void)logout;

/// 上麦
- (int)enterSeat;

/// 下麦
- (void)leavelSeat;

/// 重新通过回调 通知座位信息
- (void)renotifySeatsInfo;

/// mute/unmute本地音频采集
/// @param mute  YES表示本地音频采集空帧；NO表示恢复正常
/// @note mute是指采集和发送静音帧。采集和编码模块仍然在工作
/// @return 0表示成功放入队列，-1表示被拒绝
- (int)muteLocalMic:(BOOL)mute;

/// mute/unmute远端的所有音频track的播放
/// @param enable    YES表示停止播放；NO表示恢复播放
/// @return 0表示Success 非0表示Failure
/// @note 拉流和解码不受影响。支持joinChannel之前和之后设置

- (int)muteAllRemoteAudioPlaying:(BOOL)enable;

/// 播放背景音乐
/// @param filePath 文件路径
/// @param publish 是否推送远端
- (int)startAudioAccompanyWithFile:(NSString *)filePath
                           publish:(BOOL)publish;

/// 停止播放背景音乐
- (int)stopAudioAccompany;

/// 设置背景音乐音量
/// @param volume 音量 0~100
- (int)setAudioAccompanyVolume:(NSInteger)volume;

/// 播放音效
/// @param soundId 音效id
/// @param filePath 资源路径
/// @param publish 是否推送远端
- (int)playEffectSoundtWithSoundId:(NSInteger)soundId
                          filePath:(NSString *)filePath
                           publish:(BOOL)publish;

/// 停止播放音效
/// @param soundId 音效id
- (int)stopAudioEffectWithSoundId:(NSInteger)soundId;

/// 设置音效的音量
/// @param soundId 音效id
/// @param volume 音量 0~100
- (int)setAudioEffectPlayoutVolumeWithSoundId:(NSInteger)soundId
                                       volume:(NSInteger)volume;

/// 是否开启耳返
/// @param enable YES/NO
- (int)enableEarBack:(BOOL)enable;

/// 设置音效混响模式
/// @param mode 混响模式
- (int)setAudioEffectReverbMode:(AliRtcAudioEffectReverbMode)mode;

/// 设置变声
/// @param mode 变声类型
- (int)setAudioEffectVoiceChangerMode:(AliRtcAudioEffectVoiceChangerMode)mode;

@end

NS_ASSUME_NONNULL_END
