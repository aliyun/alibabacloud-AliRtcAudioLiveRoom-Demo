//
//  AliBaseHttpClient.m
//  RTCDemo
//
//  Created by Aliyun on 2017/8/18.
//  Copyright © 2017年 mt. All rights reserved.
//

#import "AliBaseHttpClient.h"
#import "AppConfig.h" 

@implementation AliBaseHttpClient

+ (AliBaseHttpClient *)client
{
    static dispatch_once_t once;
    static AliBaseHttpClient *client = nil;
    dispatch_once(&once,^{
        client = [[AliBaseHttpClient alloc]init];
    });
    return client;
}

- (void)httpGETWithHost:(NSString *)host
                  param:(NSDictionary *)param
                  block:(void (^)(NSDictionary *response, NSError *err))block
{
    
    NSMutableURLRequest *request=[NSMutableURLRequest requestWithURL:[self httpUrl:host param:param]];
    request.HTTPMethod=@"GET";
    
    NSDictionary *appInfo = [[NSBundle mainBundle] infoDictionary];
    NSString *app_Name = [[appInfo objectForKey:@"CFBundleDisplayName"] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    NSString *app_Version = [appInfo objectForKey:@"CFBundleShortVersionString"];
    app_Version = [app_Version stringByReplacingOccurrencesOfString:@"." withString:@""];
    NSString *bundleId = [[NSBundle mainBundle] bundleIdentifier];
    [request setValue:app_Name forHTTPHeaderField:@"appName"];
    [request setValue:app_Version forHTTPHeaderField:@"appVersionCode"];
    [request setValue:bundleId forHTTPHeaderField:@"bundleId"];
    [request setTimeoutInterval:8];
    NSURLSession *session=[NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration] delegate:(id<NSURLSessionDelegate>)self delegateQueue:[[NSOperationQueue alloc] init]];
    
    NSURLSessionDataTask *dataTask=[session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        if (error) {
            block(nil,error);
        }else{
            
            NSError *err;
            NSDictionary *responseDic = [NSJSONSerialization JSONObjectWithData:data options:0 error:&err];
            if (err) {
                NSLog(@"NSJSONSerialization error = %@",err.description);
                block(nil,err);
            }else{
                int code = [responseDic[@"code"] intValue];
                if (code == 200 || [responseDic[@"data"] isKindOfClass:[NSDictionary class]]) {
                    block(responseDic[@"data"],nil);
                } else {
                    NSString *message =responseDic[@"message"];
                    NSError *serverError = [NSError errorWithDomain:message code:code userInfo:nil];
                    block(nil,serverError);
                }
                
            }
        }
    }];
    [dataTask resume];
}

- (void)URLSession:(NSURLSession *)session didReceiveChallenge:(NSURLAuthenticationChallenge *)challenge completionHandler:(void (^)(NSURLSessionAuthChallengeDisposition, NSURLCredential *))completionHandler
{
    if ([challenge.protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust]) {
        NSURLCredential *card = [[NSURLCredential alloc]initWithTrust:challenge.protectionSpace.serverTrust];
        completionHandler(NSURLSessionAuthChallengeUseCredential , card);
    }
}

- (NSURL *)httpUrl:(NSString *)host param:(NSDictionary *)param
{
    NSMutableString *httpBodyString = [kBaseUrl_1v1Audio mutableCopy];
    
    [httpBodyString appendString:host];
    
    NSArray *arry = [param allKeys];
    for (NSUInteger i = 0; i< arry.count; i++) {
        NSString *key = arry[i];
        if (i == 0) {
            [httpBodyString appendFormat:@"?%@=%@",key,param[key]];
        }else{
            [httpBodyString appendFormat:@"&%@=%@",key,param[key]];
        }
    }
    NSURL *url = [NSURL URLWithString:httpBodyString];
    return url;
}


@end
