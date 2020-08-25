//
//  RTCSampleRemoteUserManager.m
//  RtcSample
//
//  Created by Aliyun on 2019/3/22.
//  Copyright © 2019年 com.Alibaba. All rights reserved.
//

#import "RTCRemoteUserManager.h"
#import "RTCRemoteUserModel.h"

@interface RTCRemoteUserManager ()

/**
 @brief 远端用户
 */
@property(nonatomic, strong) NSMutableArray *remoteUsers;

/**
 @brief 操作锁
 */
@property(nonatomic,strong) NSRecursiveLock *arrLock;


@end


@implementation RTCRemoteUserManager

+ (instancetype)shareManager {
    static RTCRemoteUserManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (manager == nil) {
            manager = [[self alloc] init];
        }
    });
    return manager;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _remoteUsers = [NSMutableArray array];
        _arrLock = [[NSRecursiveLock alloc] init];
    }
    return self;
}


- (void)updateRemoteUser:(NSString *)uid forTrack:(AliRtcVideoTrack)track {
    
    [_arrLock lock];
    NSInteger index = -1;
    if (track == AliRtcVideoTrackBoth) {
        RTCRemoteUserModel *cameraModel = [self findUser:uid forTrack:AliRtcVideoTrackCamera];
        RTCRemoteUserModel *screenModel = [self findUser:uid forTrack:AliRtcVideoTrackScreen];
         
        if (cameraModel) {
            index = [self.remoteUsers indexOfObject:cameraModel];
            [self.remoteUsers removeObject:cameraModel];
        }
        if (!screenModel) {
            screenModel = [self createModel:uid forTrack:AliRtcVideoTrackScreen];
            
            if (index >= 0) {
                [self.remoteUsers insertObject:screenModel atIndex:index];
            }else{
                [self.remoteUsers addObject:screenModel];
            }
        }
    }else if (track == AliRtcVideoTrackScreen) {
        RTCRemoteUserModel *cameraModel = [self findUser:uid forTrack:AliRtcVideoTrackCamera];
        RTCRemoteUserModel *screenModel = [self findUser:uid forTrack:AliRtcVideoTrackScreen];
        if (cameraModel) {
            index = [self.remoteUsers indexOfObject:cameraModel];
            [self.remoteUsers removeObject:cameraModel];
        }
        if (!screenModel) {
            screenModel = [self createModel:uid forTrack:AliRtcVideoTrackScreen];
            if (index >= 0) {
                [self.remoteUsers insertObject:screenModel atIndex:index];
            }else{
                [self.remoteUsers addObject:screenModel];
            }
        }
    }else if (track == AliRtcVideoTrackCamera){
        RTCRemoteUserModel *cameraModel = [self findUser:uid forTrack:AliRtcVideoTrackCamera];
        RTCRemoteUserModel *screenModel = [self findUser:uid forTrack:AliRtcVideoTrackScreen];
        if (screenModel) {
            index = [self.remoteUsers indexOfObject:screenModel];
            [self.remoteUsers removeObject:screenModel];
        }
        if (!cameraModel) {
            cameraModel = [self createModel:uid forTrack:AliRtcVideoTrackCamera];
            if (index >= 0) {
                [self.remoteUsers insertObject:cameraModel atIndex:index];
            }else{
                [self.remoteUsers addObject:cameraModel];
            }
        }
    }
    [_arrLock unlock];
}

- (AliRenderView *)cameraView:(NSString *)uid {
    AliRenderView *rendView = nil;
    [_arrLock lock];
    for (RTCRemoteUserModel *model in self.remoteUsers) {
        if ([model.uid isEqualToString:uid] && model.track == AliRtcVideoTrackCamera) {
            rendView = model.view;
        }
    }
    [_arrLock unlock];
    return rendView;
}

- (AliRenderView *)screenView:(NSString *)uid {
    AliRenderView *rendView = nil;
    [_arrLock lock];
    for (RTCRemoteUserModel *model in self.remoteUsers) {
        if ([model.uid isEqualToString:uid] && model.track == AliRtcVideoTrackScreen) {
            rendView = model.view;
        }
    }
    [_arrLock unlock];
    return rendView;
}

- (void)remoteUserOffLine:(NSString *)uid {
    [_arrLock lock];
    for (int i = 0; i < self.remoteUsers.count; i++) {
        RTCRemoteUserModel *model = self.remoteUsers[i];
        if ([model.uid isEqualToString:uid]) {
            [self.remoteUsers removeObject:model];
            i--;
        }
    }
    [_arrLock unlock];
}


- (NSArray *)allOnlineUsers {
    return self.remoteUsers;
}

- (void)removeAllUser {
    [_arrLock lock];
    [self.remoteUsers removeAllObjects];
    [_arrLock unlock];
}

- (void)removeUser:(RTCRemoteUserModel*)model {
    [_arrLock lock];
    [self.remoteUsers removeObject:model];
    [_arrLock unlock];
}



#pragma mark - private


/**
 @brief 创建用户流model
 
 @param uid 用户ID
 @param track 流类型
 @return 用户流model
 */
- (RTCRemoteUserModel *)createModel:(NSString *)uid forTrack:(AliRtcVideoTrack)track {
    if (uid.length == 0) {
        return nil;
    }
    RTCRemoteUserModel *model = [[RTCRemoteUserModel alloc] init];
    model.uid   = uid;
    model.track = track;
    model.view  = [[AliRenderView alloc] init];
    return model;
}



/**
 @brief 查找用户流
 
 @param uid 用户ID
 @param track 流类型
 @return 用户流model
 */
- (RTCRemoteUserModel *)findUser:(NSString *)uid forTrack:(AliRtcVideoTrack)track {
    
    for (RTCRemoteUserModel *model in self.remoteUsers) {
        if ([model.uid isEqualToString:uid] && (model.track == track)) {
            return model;
        }
    }
    return nil;
}

@end
