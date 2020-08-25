//
//  AudioRoomLoginApi.h
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/7.
//

#import <Foundation/Foundation.h>
#import <AliRTCSdk/AliRTCSdk.h>



NS_ASSUME_NONNULL_BEGIN

@interface AudioRoomApi : NSObject

/// 获取随机授AuthInfo
/// @param handler 回调处理
+ (void)randomAuthInfo:(void(^)(AliRtcAuthInfo *info,NSString *nickName,NSString *errorMsg)) handler;

/// 通过channelId 生成AuthInfo
/// @param channelId 频道ID
/// @param handler 回调处理
+ (void)authInfo:(NSString  *__nullable)channelId
        complete:(void(^)(AliRtcAuthInfo *info,NSString *nickName,NSString *errorMsg)) handler;

/// 加入房间成功后通知服务端
/// @param channelId  频道ID
/// @param userId 用户ID
/// @param nickName 昵称
/// @param handler 回调处理
+ (void)joinChannelSuccess:(NSString *)channelId
                    userId:(NSString *)userId
                  nickName:(NSString *)nickName
                  complete:(void(^)(NSString *errormsg))handler;

/// 查询房间主播人数
/// @param channel 频道ID
/// @param handler 回调处理
+ (void)numberOfUsersInChannel:(NSString *)channel
                      complete:(void(^)(NSInteger numbers,NSString *error))handler;

@end

NS_ASSUME_NONNULL_END
