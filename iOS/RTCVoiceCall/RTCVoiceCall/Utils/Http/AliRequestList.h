//
//  AliRequestList.h
//  RTCDemo
//
//  Created by Aliyun on 2017/8/18.
//  Copyright © 2017年 mt. All rights reserved.
//


#import "AliBaseHttpClient.h"

@interface AliRequestList : NSObject

/**
 @brief 用户登陆

 @param param 登陆参数
 @param block 登陆结果回调
 */
+ (void)userLoginParam:(NSDictionary *)param block:(void (^)(NSDictionary *loginModel, NSError *error))block;



/// 查询频道实时在线用户列表
/// @param param 查询参数
/// @param block 结果回调

+ (void)userListParam:(NSDictionary *)param block:(void (^)(NSDictionary *response, NSError *error))block;


+ (void)randomUser:(void (^)(NSDictionary *response, NSError *error))block;



+ (void)channelStartTimeParam:(NSDictionary *)param block:(void (^)(NSDictionary *response, NSError *error))block;
@end
