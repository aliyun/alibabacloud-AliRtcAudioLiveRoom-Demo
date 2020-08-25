//
//  AudioRoomLoginApi.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/7.
//

#import "AudioRoomApi.h"
#import "AppConfig.h"
#import "NetworkManager.h"

@implementation AudioRoomApi

+ (void)randomAuthInfo:(void(^)(AliRtcAuthInfo *info,NSString *nickName,NSString *errorMsg)) handler
{
    [self authInfo:nil complete:handler];
}

+ (void)authInfo:(NSString * __nullable)channelId
        complete:(void(^)(AliRtcAuthInfo *info,NSString *nickName,NSString *errorMsg)) handler
{
    NSString *url = [kBaseUrl_Chatroom stringByAppendingString:@"user/randomUser"];
    
    NSDictionary *param = nil;
    if (channelId) {
        param = @{@"channelId":channelId};
    }
    
    [NetworkManager GET:url
             parameters:param
      completionHandler:^(NSString * _Nullable errString,NSDictionary * _Nullable resultDic)
     {
        if(!errString) {
            AliRtcAuthInfo *authInfo = [[AliRtcAuthInfo alloc] init];
            authInfo.appid = resultDic[@"appid"];
            authInfo.user_id = resultDic[@"userid"];
            authInfo.channel = resultDic[@"channelId"];
            authInfo.nonce = resultDic[@"nonce"];
            authInfo.timestamp = [resultDic[@"timestamp"] longLongValue];
            authInfo.token = resultDic[@"token"];
            authInfo.gslb = resultDic[@"gslb"];
            authInfo.agent = resultDic[@"agent"];
            handler(authInfo,resultDic[@"userName"],nil);
        }else{
            handler(nil,nil,errString);
        }
    }];
}

+ (void)joinChannelSuccess:(NSString *)channelId
                    userId:(NSString *)userId
                  nickName:(NSString *)nickName
                  complete:(void(^)(NSString *errormsg))handler
{
    NSString *url = [kBaseUrl_Chatroom stringByAppendingString:@"user/joinSuccess"];
    
    NSMutableDictionary *param = @{}.mutableCopy;
    if (channelId) {
        [param setObject:channelId forKey:@"channelId"];
    }
    
    if (userId) {
        [param setObject:userId forKey:@"userid"];
    }
    
    if (nickName) {
        [param setObject:nickName forKey:@"userName"];
    }
    
    [NetworkManager GET:url
             parameters:param
      completionHandler:^(NSString * _Nullable errString, NSDictionary * _Nullable resultDic)
     {
        handler(errString);
    }];
}

+ (void)numberOfUsersInChannel:(NSString *)channel
                      complete:(void(^)(NSInteger numbers,NSString *error))handler
{
    NSString *url = [kBaseUrl_Chatroom stringByAppendingString:@"user/describeChannelUsers"];
    
    NSMutableDictionary *param = @{}.mutableCopy;
    if (channel) {
        [param setObject:channel forKey:@"channelId"];
    }
    
    [NetworkManager GET:url
             parameters:param
      completionHandler:^(NSString * _Nullable errString, id  _Nullable result)
     {
        if (errString) {
            handler(-1,errString);
        }else{
            NSInteger number = [result[@"interactiveUserNum"] integerValue];
            handler(number,nil);
        }
    }];
}
@end
