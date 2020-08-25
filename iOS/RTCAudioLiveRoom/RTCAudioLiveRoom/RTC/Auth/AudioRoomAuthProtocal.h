//
//  AudioRoomAuthProtocal.h
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/28.
//

#ifndef AudioRoomAuthProtocal_h
#define AudioRoomAuthProtocal_h
#import <AliRTCSdk/AliRTCSdk.h>
@protocol AudioRoomAuthProtocal <NSObject>

/// 通过channelId 生成AuthInfo
/// @param params 参数
/// @param handler 回调处理
- (void)authInfo:(NSDictionary *)params
        complete:(void(^)(AliRtcAuthInfo *info,NSString *nickName,NSString *errorMsg)) handler;
@end

#endif /* AudioRoomAuthProtocal_h */
