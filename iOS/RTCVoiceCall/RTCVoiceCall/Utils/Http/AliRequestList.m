//
//  AliRequestList.m
//  RTCDemo
//
//  Created by Aliyun on 2017/8/18.
//  Copyright © 2017年 mt. All rights reserved.
//

#import "AliRequestList.h"
#import "AliBaseHttpClient.h"

@implementation AliRequestList

+ (void)userLoginParam:(NSDictionary *)param block:(void (^)(NSDictionary *loginModel, NSError *error))block
{
    [[AliBaseHttpClient client] httpGETWithHost:@"app/token" param:param block:^(NSDictionary *responseDic, NSError *error){
        if (error)
        {
            if(block)
                block(nil,error);
            return ;
        }
        if(block)
            block(responseDic,error);
    }];
}

+ (void)userListParam:(NSDictionary *)param block:(void (^)(NSDictionary *response, NSError *error))block
{
    [[AliBaseHttpClient client] httpGETWithHost:@"app/descChannelUsers" param:param block:^(NSDictionary *responseDic, NSError *error){
        if (error)
        {
            if(block)
                block(nil,error);
            return ;
        }
        if(block)
            block(responseDic,error);
    }];
}

+ (void)randomUser:(void (^)(NSDictionary *response, NSError *error))block{
    [[AliBaseHttpClient client] httpGETWithHost:@"user/randomUser" param:nil block:^(NSDictionary *responseDic, NSError *error){
        if (error)
        {
            if(block)
                block(nil,error);
            return ;
        }
        if(block)
            block(responseDic,error);
    }];
}


+ (void)channelStartTimeParam:(NSDictionary *)param block:(void (^)(NSDictionary *response, NSError *error))block {
    [[AliBaseHttpClient client] httpGETWithHost:@"app/descChannelStartTime" param:param block:^(NSDictionary *responseDic, NSError *error){
        if (error)
        {
            if(block)
                block(nil,error);
            return ;
        }
        if(block)
            block(responseDic,error);
    }];
}
@end
