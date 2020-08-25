//
//  RTCSampleRemoteUserManager.h
//  RtcSample
//
//  Created by Aliyun on 2019/3/22.
//  Copyright © 2019年 com.Alibaba. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AliRTCSdk/AliRtcEngine.h>

@class RTCRemoteUserModel;

NS_ASSUME_NONNULL_BEGIN

@interface RTCRemoteUserManager : NSObject


/**
 @brief 实例

 @return 实例
 */
+ (instancetype)shareManager;

/**
 @brief 更新用户视频流

 @param uid 用户ID
 @param track 视频流
 */
- (void)updateRemoteUser:(NSString *)uid forTrack:(AliRtcVideoTrack)track;


/**
 @brief 获取用户camera renderview
 @param uid 用户ID
 @return renderview
 */
- (AliRenderView *)cameraView:(NSString *)uid;


/**
 @brief 获取用户screen renderview

 @param uid 用户ID
 @return renderview
 */
- (AliRenderView *)screenView:(NSString *)uid;

/**
 @brief 获取所有在线用户
 
 @return 所有在线用户
 */
- (NSArray *)allOnlineUsers;


/**
 @brief 远端用户下线
 
 @param uid 用户ID
 */
- (void)remoteUserOffLine:(NSString *)uid;


/**
 @brief 移除房间所有用户
 */
- (void)removeAllUser;

//删除单个model
- (void)removeUser:(RTCRemoteUserModel*)model;

@end

NS_ASSUME_NONNULL_END
