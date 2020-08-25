//
//  AudioRoomSeatFetcher.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/14.
//

#import "AudioRoomSeatsInfoFether.h"
#import "AppConfig.h"
#import "NetworkManager.h"
#import "MJExtension.h"
#import "SeatInfo.h"

//工厂类获取
@implementation AudioRoomSeatsInfoFether

- (void)getSeatList:(NSDictionary *)params
           complete:(void(^)(NSArray *seats,NSString *error))handler
{
    NSString *url = [kBaseUrl_Chatroom stringByAppendingString:@"user/getSeatList"];
    
    [NetworkManager GET:url
             parameters:params
      completionHandler:^(NSString * _Nullable errString, id  _Nullable result)
     {
        if (errString)
        {
            handler(nil,errString);
        } else {
            if ([result isKindOfClass:[NSArray class]])
            {
                NSLog(@"我的日志：getSeatList");
                NSArray <SeatInfo *>*array = [SeatInfo mj_objectArrayWithKeyValuesArray:result];
                NSLog(@"%@",array);
                handler(array,nil);
            } else {
                handler(nil,@"没有获取到人数");
            }
        }
    }];
}

@end
