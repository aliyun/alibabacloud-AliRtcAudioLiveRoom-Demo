#import "VideoCallUserAuthrization.h"
#import "AliRequestList.h"

@interface VideoCallUserAuthrization()

@end

static NSString *userToken;

static NSDateFormatter *df;

@implementation VideoCallUserAuthrization

+ (void)getPassportFromAppServer:(NSString *)channelName block:(void (^)(AliRtcAuthInfo *AuthInfo, NSError *error))block{
    NSDictionary *param = @{ @"channelId":channelName};
    [AliRequestList userLoginParam:param block:^(NSDictionary *joinModel, NSError *err){
        if (err) {
             block(nil,err);
        }
        else{
            NSMutableDictionary *loginDic = [[NSMutableDictionary alloc]init];
            NSDictionary *dataDic = joinModel;
            NSArray *keysArray = [dataDic allKeys];
            for (NSUInteger i = 0; i < keysArray.count; i++) {
                NSString *key = keysArray[i];
                NSString *value = dataDic[key];
                [loginDic setObject:value forKey:key];
            }
            
            AliRtcAuthInfo * info = [[AliRtcAuthInfo alloc]init];
            info.channel   = channelName;
            info.appid     = loginDic[@"appid"];
            info.nonce     = loginDic[@"nonce"];
            info.user_id   = loginDic[@"userid"];
            info.token     = loginDic[@"token"];
            info.timestamp = [loginDic[@"timestamp"] longLongValue];
            info.gslb      = loginDic[@"gslb"];
            block(info,nil);
        }
    }];
}

+ (void)userListInChannel:(NSString *)channelId  block:(void (^)(NSArray *users, NSError *error))block{
    NSDictionary *param = @{@"channelId":channelId};
    [AliRequestList userListParam:param block:^(NSDictionary *response, NSError *error) {
        if (error) {
            block(nil,error);
        }else{
             NSArray *users = response[@"userList"];
            block(users,nil);
        }
    }];
}


+ (void)channelStartTimeWithAppId:(NSString *)appId channel:(NSString *)channel block:(void (^)(NSDate *startTime, NSError *error))block {
    
    NSDictionary *param = @{@"channelId":channel};
    [AliRequestList channelStartTimeParam:param block:^(NSDictionary *response, NSError *error) {
        if (error) {
            block(0,error);
        }else{
             NSString *dateStr = response[@"channelStartTimeUtc"];

            static NSDateFormatter *df = nil;
            if (df == nil) {
                df = [[NSDateFormatter alloc] init];
                [df setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZ"];
                NSTimeZone *localTimeZone = [NSTimeZone localTimeZone];
                [df setTimeZone:localTimeZone];
            }
            NSDate *date = [df dateFromString:dateStr];
            block(date,nil);
        }
    }];
}
@end
