#import <UIKit/UIKit.h>
#import <AliRTCSdk/AliRTCSdk.h>

#define ALIVC_USERTOKEN @"user_token"
#define ALIVC_USERNAME @"user_name"

@interface VideoCallUserAuthrization : NSObject


/**
 @brief 这是app厂商实现app server查询的示例代码

 @param channelName 频道名称
 @param token 用户名
 */

+ (void)getPassportFromAppServer:(NSString *)channelNam block:(void (^)(AliRtcAuthInfo *AuthInfo, NSError *error))block;


+ (void)userListInChannel:(NSString *)channelId  block:(void (^)(NSArray *users, NSError *error))block;


+ (void)channelStartTimeWithAppId:(NSString *)appId channel:(NSString *)channel block:(void (^)(NSDate *startTime, NSError *error))block;

@end
