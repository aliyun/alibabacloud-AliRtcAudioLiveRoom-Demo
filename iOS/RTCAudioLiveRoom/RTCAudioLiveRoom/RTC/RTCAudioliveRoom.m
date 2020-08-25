//
//  RTCAudioliveRoomManager.m
//  LectureHall
//
//  Created by Aliyun on 2020/6/15.
//  Copyright © 2020 alibaba. All rights reserved.
//

#import "RTCAudioliveRoom.h"
#import "SeatInfo.h"
#import "AudioRoomFetcherFactory.h"

static dispatch_once_t onceToken;
static RTCAudioliveRoom *manager = nil;

@interface RTCAudioliveRoom()<AliRtcEngineDelegate>

@property (copy, nonatomic) NSString *displayName;
@property (strong, nonatomic) AliRtcEngine *engine;
@property (strong, nonatomic) AliRtcAuthInfo * info;
@property (strong, nonatomic) id<AudioRoomSeatsInfoProtocal> seatsInfofether;
@property (strong, nonatomic) id<AudioRoomAuthProtocal> authorizationfether;
@property (strong, nonatomic) NSMutableDictionary *seatsDict;
@end

@implementation RTCAudioliveRoom

+ (RTCAudioliveRoom *) sharedInstance{
    dispatch_once(&onceToken, ^{
        manager = [[super allocWithZone:NULL] init];
    });
    return manager;
}

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    return [RTCAudioliveRoom sharedInstance];
}

- (id)copyWithZone:(nullable NSZone *)zone {
    return [RTCAudioliveRoom sharedInstance];
}

- (id)mutableCopyWithZone:(nullable NSZone *)zone {
    return [RTCAudioliveRoom sharedInstance];
}


#pragma public method

- (void)logout
{
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        [self setAudioEffectVoiceChangerMode:0];
        [self setAudioEffectReverbMode:0];
        [self.engine unSubscribeAudioData:AliRtcAudiosourceVolume];
        [self.engine leaveChannel];
    });
}

-(void)destroySharedInstance
{
    if(_engine){
        [AliRtcEngine destroy];
        _engine = nil;
    }
    onceToken = 0;
    manager = nil;
}

- (void)login:(NSString *)channelId
         name:(NSString *)name
         role:(AliRtcClientRole)role
     complete:(void(^)(AliRtcAuthInfo *authInfo, NSInteger errorCode))handler
{
    [self.authorizationfether authInfo:@{@"channelId":channelId}
                              complete:^(AliRtcAuthInfo * _Nonnull info, NSString * _Nonnull nickName, NSString * _Nonnull errorMsg)
     {
        if (!errorMsg)
        {
            self.info = info;
            self.displayName = name;
            [self.engine setClientRole:role];
            [self emptySeatsDict];
            [self.engine joinChannel:info name:name onResult:^(NSInteger errCode)
             {
                if (errCode == 0)
                {
                    //订阅音量
                    [self.engine subscribeAudioData: AliRtcAudiosourceVolume];
                    //设置音量回调频率和平滑系数
                    [self.engine setVolumeCallbackIntervalMs:160 smooth:3 reportVad:1];
                    //如果是主播 则开始推流
                    if(role == AliRtcClientRoleInteractive)
                    {
                        [self.engine configLocalAudioPublish:YES];
                        [self.engine publish:^(int errCode) {
                            //主播身份切换成功通知
                        }];
                    }
                }
                dispatch_async(dispatch_get_main_queue(), ^{
                    handler(info,0);
                });
                [self refreshSeats];
            }];
        } else {
            handler(nil,-1);
        }
    }];
}

- (int)muteLocalMic:(BOOL)mute
{
    int result =  [self.engine muteLocalMic:mute];
    if (!result)
    {
        [self onUserAudioMuted:self.info.user_id audioMuted:mute];
    }
    
    return result;
}

- (int)muteAllRemoteAudioPlaying:(BOOL)enable
{
    return  [self.engine muteAllRemoteAudioPlaying:enable];
}

- (int)startAudioAccompanyWithFile:(NSString *)filePath
                           publish:(BOOL)publish
{
    return [self.engine startAudioAccompanyWithFile:filePath onlyLocalPlay:!publish replaceMic:NO loopCycles:1];
}

- (int)setAudioAccompanyVolume:(NSInteger)volume
{
    [self.engine setAudioAccompanyPlayoutVolume:volume];
    return [self.engine setAudioAccompanyPublishVolume:volume];
}

- (int)stopAudioAccompany {
    return [self.engine stopAudioAccompany];
}

- (int)playEffectSoundtWithSoundId:(NSInteger)soundId
                          filePath:(NSString *)filePath
                           publish:(BOOL)publish
{
    [self.engine preloadAudioEffectWithSoundId:soundId filePath:filePath];
    return [self.engine playAudioEffectWithSoundId:soundId filePath:filePath cycles:1 publish:publish];
}

- (int)stopAudioEffectWithSoundId:(NSInteger)soundId
{
    return [self.engine stopAudioEffectWithSoundId:soundId];
}

- (int)setAudioEffectPlayoutVolumeWithSoundId:(NSInteger)soundId
                                       volume:(NSInteger)volume
{
    [self.engine setAudioEffectPlayoutVolumeWithSoundId:soundId volume:volume];
    return [self.engine setAudioEffectPublishVolumeWithSoundId:soundId volume:volume];
}

- (int)enableEarBack:(BOOL)enable
{
    return [self.engine enableEarBack:enable];
}

- (int)setAudioEffectReverbMode:(AliRtcAudioEffectReverbMode)mode
{
    //关闭变声
    [self.engine setAudioEffectVoiceChangerMode:AliRtcAudioEffectvVoiceChanger_OFF];
    return [self.engine setAudioEffectReverbMode:mode];
}

- (int)setAudioEffectVoiceChangerMode:(AliRtcAudioEffectVoiceChangerMode)mode
{
    //关闭混响
    [self.engine setAudioEffectReverbMode:AliRtcAudioEffectReverb_Off];
    return [self.engine setAudioEffectVoiceChangerMode:mode];;
}

#pragma private methods

- (void)initializeSDK
{
    //高音质模式
    NSMutableDictionary *extraDic = [[NSMutableDictionary alloc] init];
    [extraDic setValue:@"ENGINE_HIGH_QUALITY_MODE" forKey:@"user_specified_engine_mode"];
    [extraDic setValue:@"SCENE_MUSIC_MODE" forKey:@"user_specified_scene_mode"];
    
    NSError *error = nil;
    NSData *json = [NSJSONSerialization dataWithJSONObject:extraDic options:NSJSONWritingPrettyPrinted error:&error];
    
    NSString *string = [[NSString alloc] initWithData:json encoding:NSUTF8StringEncoding];
    
    // 创建SDK实例，注册delegate，extras可以为空
    _engine = [AliRtcEngine sharedInstance:self extras:string];
    //使用扬声器
    [_engine enableSpeakerphone:YES];
    //设置播放音量
    [_engine setPlayoutVolume:130];
    //纯音频模式
    [_engine setAudioOnlyMode:YES];
    //频道模式
    [_engine setChannelProfile:AliRtcInteractivelive];
    //自动拉流 手动推流
    [_engine setAutoPublish:NO withAutoSubscribe:YES];
}

- (int)setClientRole:(AliRtcClientRole)role
{
    return [self.engine setClientRole:role];
}

-(NSString *)displayName:(NSString *)userid {
    if ([userid isEqualToString:self.info.user_id]) {
        return self.displayName;
    }
    NSLog(@"用户名称:%@-----%@",userid,[self.engine getUserInfo:userid][@"displayName"]);
    return [self.engine getUserInfo:userid][@"displayName"];
}

- (AliRtcClientRole)getCurrentClientRole {
    return [self.engine getCurrentClientRole];
}

- (AliRtcEngine *)engine {
    if (!_engine) {
        [self initializeSDK];
    }
    return _engine;
}


#pragma mark - AliRtcEngineDelegate

- (void)onPublishResult:(int)result
            isPublished:(BOOL)isPublished
{
    if ([self.delegate respondsToSelector:@selector(onPublishResult:isPublished:)])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.delegate onPublishResult:result
                               isPublished:isPublished];
        });
    }
}

- (void)onAudioVolumeCallback:(AliRtcAudioSource)audioSource
                userAudioinfo:(NSArray <AliRtcUserVolumeInfo *> *)array
{
    for (AliRtcUserVolumeInfo *roomInfo in array)
    {
        //uid 为1 代表的是总音量
        if([roomInfo.uid isEqualToString:@"1"])
        {
            continue;
        }
        //如果uid是0   或者不存在表示的是自己
        if([roomInfo.uid isEqualToString:@"0"]||roomInfo.uid.length == 0)
        {
            roomInfo.uid = self.info.user_id;
        }
        
        //查找seat信息
        SeatInfo *seat = [self getSeat:roomInfo.uid];
        
        //如果不存在 添加到数组中
        if (!seat)
        {
            [self updateSpeaking:roomInfo.speech_state userId:roomInfo.uid];
            return;
        }
        
        //如果说话状态变化了 并且正在座位上
        if (seat.speaking != roomInfo.speech_state && seat.seatIndex >=0 )
        {
            [self updateSpeaking:roomInfo.speech_state userId:roomInfo.uid];
            if ([self.delegate respondsToSelector:@selector(onSeatVolumeChanged:isSpeaking:)])
            {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self.delegate onSeatVolumeChanged:seat.seatIndex isSpeaking:seat.speaking];
                });
            }
        }
    }
}

- (void)onUserAudioMuted:(NSString *)uid
              audioMuted:(BOOL)isMute {
    
    [self updateMute:isMute  userId:uid];
    SeatInfo *seat = [self getSeat:uid];
    //如果座位号大于等于0 则在麦上 回调通知
    if (seat.seatIndex >= 0) {
        if ([self.delegate respondsToSelector:@selector(onSeatMutedChanged:mute:)])
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.delegate onSeatMutedChanged:seat.seatIndex mute:isMute];
            });
        }
    }
}

- (void)onBye:(int)code {
    if (code == 2)
    {
        if ([self.delegate respondsToSelector:@selector(onRoomDestory)])
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.delegate onRoomDestory];
            });
        }
    }
}

- (void)onAudioPlayingStateChanged:(AliRtcAudioPlayingStateCode)playState errorCode:(AliRtcAudioPlayingErrorCode)errorCode {
    if ([self.delegate respondsToSelector:@selector(onAudioPlayingStateChanged:errorCode:)])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.delegate onAudioPlayingStateChanged:playState
                                            errorCode:errorCode];
        });
    }
}

- (void)onOccurError:(int)error
{
    if ([self.delegate respondsToSelector:@selector(onOccurError:)])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.delegate onOccurError:error];
        });
    }
}

- (void)onOccurWarning:(int)warn
{
    if ([self.delegate respondsToSelector:@selector(onOccurWarning:)])
    {
        [self.delegate onOccurWarning:warn];
    }
}

- (void)onNetworkQualityChanged:(NSString *)uid
               upNetworkQuality:(AliRtcNetworkQuality)upQuality
             downNetworkQuality:(AliRtcNetworkQuality)downQuality
{
    if ([self.delegate respondsToSelector:@selector(onNetworkQualityChanged:upNetworkQuality:downNetworkQuality:)])
    {
        [self.delegate onNetworkQualityChanged:uid
                              upNetworkQuality:upQuality
                            downNetworkQuality:downQuality];
    }
}

#pragma mark - 上下麦的方法

/// 上麦只需要切换角色 成功的回调是 onUpdateRoleNotifyWithOldRole
- (int)enterSeat
{
    return [self.engine setClientRole:AliRtcClientRoleInteractive];
}

///  下麦需要停止推流 成功的回调是 onUpdateRoleNotifyWithOldRole
- (void)leavelSeat
{
    __weak typeof(self) weakSelf = self;
    [self.engine configLocalAudioPublish:NO];
    [self.engine publish:^(int errCode)
     {
        if (errCode == 0) {
            [weakSelf.engine setClientRole:AliRtcClientRolelive];
        } 
    }];
}

#pragma mark - 麦序发生变化的代理

- (void)onLeaveChannelResult:(int)result
{
    if ([self.delegate respondsToSelector:@selector(onLeaveChannelResult:)])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (result == 0) {
                [self emptySeatsDict];
            }else{
                [self.engine subscribeAudioData: AliRtcAudiosourceVolume];
            }
            [self.delegate onLeaveChannelResult:result];
        });
    }
}

- (void)onUpdateRoleNotifyWithOldRole:(AliRtcClientRole)oldRole
                              newRole:(AliRtcClientRole)newRole
{
    if (newRole == AliRtcClientRoleInteractive)
    {
        //切换成主播
        [self.engine configLocalAudioPublish:YES];
        [self.engine publish:^(int errCode) {
            //主播身份切换成功通知
        }];
    }
    
    if ([self.delegate respondsToSelector:@selector(onUpdateRoleNotifyWithOldRole:newRole:)])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.delegate onUpdateRoleNotifyWithOldRole:oldRole
                                                 newRole:newRole];
        });
    }
    
    [self refreshSeats];
}

- (void)onRemoteUserOnLineNotify:(NSString *)uid
{
    [self refreshSeats];
}

- (void)onRemoteUserOffLineNotify:(NSString *)uid
{
    [self refreshSeats];
}

- (void)onRemoteTrackAvailableNotify:(NSString *)uid
                          audioTrack:(AliRtcAudioTrack)audioTrack
                          videoTrack:(AliRtcVideoTrack)videoTrack
{
#warning sdk需要优化
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW,
                                 (int64_t)(2 * NSEC_PER_SEC)),
                   dispatch_get_main_queue(), ^{
        [self refreshSeats];
    });
}

- (void)refreshSeats
{
    
    if([self.seatsInfofether respondsToSelector:@selector(getSeatList:complete:)])
    {
        [self.seatsInfofether getSeatList:@{@"channelId":self.info.channel}
                                 complete:^(NSArray * _Nonnull seats, NSString * _Nonnull error)
         {
            if([NSThread currentThread].isMainThread)
            {
                [self dealwithseats:seats];
            } else {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self dealwithseats:seats];
                    
                });
            }
        }];
    }
}
// 放到主线程中
- (void)dealwithseats:(NSArray *)seats
{
    if (!seats) {
        return;
    }
    //下麦seats查找
    //原map中有 新数组中没有
    NSArray *oldSeats = [self.seatsDict allValues];
    NSPredicate *offlinePredicate = [NSPredicate predicateWithFormat:@"NOT (SELF.userId in %@.userId)",seats];
    NSArray *offlineSeats = [oldSeats filteredArrayUsingPredicate:offlinePredicate];
    
    //处理下麦seats
    for (SeatInfo *seat in offlineSeats)
    {
        //如果是已经上麦的用户
        if(seat.seatIndex >= 0){
            [self removeSeat:seat.userId];
            if ([self.delegate respondsToSelector:@selector(onLeaveSeat:)])
            {
                [self.delegate onLeaveSeat:seat];
            }
        }
    }
    //处理上麦数据 判断是不是-1  是-1 就是上麦
    for (SeatInfo *tmpSeat in seats) {
        SeatInfo *oldSeat = [self getSeat:tmpSeat.userId];
        
        //原来userId不存在 或者seatIndex = -1  则通知业务层上麦
        if (oldSeat == nil || oldSeat.seatIndex < 0) {
            [self updateSeatIndex:tmpSeat.seatIndex userId:tmpSeat.userId];
            SeatInfo *newSeat = [self getSeat:tmpSeat.userId];
            if ([self.delegate respondsToSelector:@selector(onEnterSeat:)]) {
                [self.delegate onEnterSeat:newSeat];
            }
        }
        
    }
}

#pragma mark - map操作相关 加锁保证线程安全


/// 获取麦序
/// @param userId 用户id
- (SeatInfo *)getSeat:(NSString *)userId
{
    SeatInfo *seatInfo;
    @synchronized ([RTCAudioliveRoom class]) {
        seatInfo = self.seatsDict[userId];
        if (seatInfo.userName.length == 0) {
            seatInfo.userName = [self displayName:userId];
        }
    }
    return seatInfo;
}

/// 删除麦序
/// @param userId 用户id
- (void)removeSeat:(NSString *)userId
{
    @synchronized ([RTCAudioliveRoom class]) {
        [self.seatsDict removeObjectForKey:userId];
    }
}

/// 更新麦序
/// @param seatIndex 麦序
/// @param userId 用户id
- (void)updateSeatIndex:(NSInteger)seatIndex userId:(NSString *)userId
{
    @synchronized ([RTCAudioliveRoom class]) {
        SeatInfo *seatInfo = [self getSeat:userId];
        if (!seatInfo) {
            seatInfo = [[SeatInfo alloc] init];
            seatInfo.userId = userId;
            seatInfo.userName = [self displayName:userId];
            [self.seatsDict setObject:seatInfo forKey:userId];
        }
        seatInfo.seatIndex = seatIndex;
    }
}

/// 更新静音
/// @param mute 是否静音
/// @param userId 用户id
- (void)updateMute:(NSInteger)mute userId:(NSString *)userId
{
    @synchronized ([RTCAudioliveRoom class]) {
        SeatInfo *seatInfo = [self getSeat:userId];
        if (!seatInfo) {
            seatInfo = [[SeatInfo alloc] init];
            seatInfo.userId = userId;
            seatInfo.userName = [self displayName:userId];
            [self.seatsDict setObject:seatInfo forKey:userId];
        }
        seatInfo.muteMic = mute;
    }
}

/// 更新是否正在说话
/// @param speaking 正在说话
/// @param userId 用户id
- (void)updateSpeaking:(NSInteger)speaking userId:(NSString *)userId
{
    @synchronized ([RTCAudioliveRoom class]) {
        SeatInfo *seatInfo = [self getSeat:userId];
        if (!seatInfo) {
            seatInfo = [[SeatInfo alloc] init];
            seatInfo.userId = userId;
            seatInfo.userName = [self displayName:userId];
            [self.seatsDict setObject:seatInfo forKey:userId];
        }
        seatInfo.speaking = speaking;
    }
}

/// 清空map
- (void)emptySeatsDict
{
    @synchronized ([RTCAudioliveRoom class]) {
        [self.seatsDict removeAllObjects];
    }
}

/// 初始化map
- (NSMutableDictionary *)seatsDict
{
    @synchronized ([RTCAudioliveRoom class]) {
        if (!_seatsDict) {
            _seatsDict = [@{} mutableCopy];
        }
    }
    
    return _seatsDict;
}

- (void)renotifySeatsInfo {
    @synchronized ([RTCAudioliveRoom class]) {
        //如果设置了代理 则把数据都推一遍
        [self.seatsDict enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
            if ([self.delegate respondsToSelector:@selector(onEnterSeat:)]) {
                SeatInfo *seat = obj;
                if (seat.seatIndex>=0) {
                    [self.delegate onEnterSeat:obj];
                }
            }
        }];
    }
}

- (void)setDelegate:(id<RTCAudioliveRoomDelegate>)delegate {
    _delegate = delegate;
    if (delegate) {
        [self renotifySeatsInfo];
    }
}

- (id<AudioRoomSeatsInfoProtocal>)seatsInfofether
{
#warning 修改 AudioRoomSeatsLoaderFactory 工厂类 创建自己的麦序类
    if (!_seatsInfofether) {
        _seatsInfofether = [AudioRoomFetcherFactory getSeatsInfoFetcher:KAudioRommSeatFetherDefault];
    }
    NSAssert(_seatsInfofether != nil, @"请初始化请求麦序的对象");
    return _seatsInfofether;
}

- (id<AudioRoomAuthProtocal>)authorizationfether
{
#warning 修改 AudioRoomSeatsLoaderFactory 工厂类 创建自己的麦序类
    if (!_authorizationfether) {
        _authorizationfether = [AudioRoomFetcherFactory getAuthorizationFether:KAudioRommAuthorizationFetherDefault];
    }
    NSAssert(_authorizationfether != nil, @"请初始化获取授权信息的对象");
    return _authorizationfether;
}

@end
