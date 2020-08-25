//
//  AudioRoomAuthorization.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/28.
//

#import "AudioRoomAuthorizationFether.h"
#import "AppConfig.h"
#import "NetworkManager.h"

@implementation AudioRoomAuthorizationFether

- (void)authInfo:(NSDictionary *)params
        complete:(void(^)(AliRtcAuthInfo *info,NSString *nickName,NSString *errorMsg)) handler
{
    NSString *url = [kBaseUrl_Chatroom stringByAppendingString:@"user/randomUser"];
    
//    NSDictionary *param = nil;
//    if (channelId) {
//        param = @{@"channelId":channelId};
//    }
    
    [NetworkManager GET:url
             parameters:params
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

@end
