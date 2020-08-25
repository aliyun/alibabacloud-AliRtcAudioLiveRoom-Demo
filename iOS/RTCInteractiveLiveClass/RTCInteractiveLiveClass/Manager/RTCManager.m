//
//  RTCManager.m
//  LectureHall
//
//  Created by Aliyun on 2020/6/15.
//  Copyright © 2020 alibaba. All rights reserved.
//

#import "RTCManager.h"


@interface RTCManager()

/**
 @brief SDK实例
 */
@property (nonatomic, strong) AliRtcEngine *engine;

@property (nonatomic, weak) id<AliRtcEngineDelegate> delegate;
@end

@implementation RTCManager

+ (RTCManager *) sharedManager{
    static dispatch_once_t onceToken;
    static RTCManager *manager = nil;
    dispatch_once(&onceToken, ^{
        manager = [[super allocWithZone:NULL] init];
        
    });
    return manager;
}

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    return [RTCManager sharedManager];
}

- (id)copyWithZone:(nullable NSZone *)zone {
    return [RTCManager sharedManager];
}

- (id)mutableCopyWithZone:(nullable NSZone *)zone {
    return [RTCManager sharedManager];
}


/**
 @brief 初始化SDK
 */
- (RTCManager *)initializeSDKWithDelegate:(id<AliRtcEngineDelegate>)delegate{
    self.delegate = delegate;
    //注意在sdk实例化之前设置
    [AliRtcEngine setH5CompatibleMode:YES];
    // 创建SDK实例，注册delegate，extras可以为空
    _engine = [AliRtcEngine sharedInstance:delegate extras:@""];
    //使用扬声器
    [_engine enableSpeakerphone:YES];
    [_engine setDeviceOrientationMode:AliRtcOrientationModeLandscapeLeft];
    //关闭高清预览
    [_engine enableHighDefinitionPreview:NO];
    
    [_engine setAutoPublish:YES withAutoSubscribe:NO];
    
    return self;
}



-(void)destroyEngine{
    if(_engine){
        [self.engine stopPreview];
        
        [self.engine leaveChannel];
        
        [AliRtcEngine destroy];
        _engine = nil;
    }
}

- (void)joinChannel:authInfo name:(NSString *)name success:(void(^)(void))success  error:(void(^)(NSInteger errCode))error {
    [self.engine joinChannel:authInfo name:name onResult:^(NSInteger errCode) {
        if (errCode == 0) {
            if (success) {
                success();
            }
        }else {
            if (error) {
                error(errCode);
            }
        }
    }];
}


- (void)startLiveStreamingWithAuthInfo:(AliRtcAuthInfo *)authInfo success:(void(^)(void))success  error:(void(^)(NSInteger errCode))error {
    [self.engine startLiveStreamingWithAuthInfo:authInfo onResult:^(int errCode) {
        if (errCode == 0) {
            if (success) {
                success();
            }
        }else {
            if (error) {
                error(errCode);
            }
        }
    }];
}

- (int)muteLocalMic:(BOOL)mute {
    return [self.engine muteLocalMic:mute];
}

- (int)muteLocalCamera:(BOOL)mute forTrack:(AliRtcVideoTrack)track {
    if (mute) {
        [self.engine stopPreview];
    }else{
        [self.engine startPreview];
    }
    return [self.engine muteLocalCamera:mute forTrack:track];
   
}

- (int)switchCamera {
    return [self.engine switchCamera];
}

- (int)setRemoteViewConfig:(AliVideoCanvas *)canvas uid:(NSString *)uid forTrack:(AliRtcVideoTrack)track {
    return [self.engine setRemoteViewConfig:canvas uid:uid forTrack:track];
}

- (int)setLocalViewConfig:(AliVideoCanvas *)viewConfig forTrack:(AliRtcVideoTrack)track {
    return [self.engine setLocalViewConfig:viewConfig forTrack:track];
}

- (int)stopPreview {
    return [self.engine stopPreview];
}

- (int)startPreview {
    return [self.engine startPreview];
}

- (void)configRemoteTrack:(NSString *)uid preferMaster:(BOOL)master enable:(BOOL)enable {
    [self.engine configRemoteCameraTrack:uid preferMaster:master enable:enable];
    [self.engine configRemoteScreenTrack:uid enable:enable];
    [self.engine configRemoteAudio:uid enable:enable];
    
}

- (void)subscribe:(NSString *)uid onResult:(void (^)(NSString *uid, AliRtcVideoTrack vt, AliRtcAudioTrack at))onResult {
    [self.engine subscribe:uid onResult:onResult];
}

@end
