//
//  AliBaseHttpClient.h
//  RTCDemo
//
//  Created by Aliyun on 2017/8/18.
//  Copyright © 2017年 mt. All rights reserved.
//

#import <Foundation/Foundation.h>




@interface AliBaseHttpClient : NSObject

/**
 @brief 实例化

 @return 请求实例
 */
+ (AliBaseHttpClient *)client;


/**
 @brief 请求用户author信息

 @param host 请求host
 @param param 请求参数
 @param block 请求回调
 */
- (void)httpGETWithHost:(NSString *)host param:(NSDictionary *)param block:(void (^)(NSDictionary *response, NSError *err))block;

@end

