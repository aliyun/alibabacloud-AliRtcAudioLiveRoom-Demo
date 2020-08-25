//
//  AudioRoomSeatsLoaderFactory.m
//  Pods
//
//  Created by Aliyun on 2020/7/22.
//

#import "AudioRoomFetcherFactory.h"
#import "AudioRoomSeatsInfoFether.h"
#import "AudioRoomAuthorizationFether.h"
@implementation AudioRoomFetcherFactory

+ (id<AudioRoomSeatsInfoProtocal>)getSeatsInfoFetcher:(NSString *)type
{
    if ([type isEqualToString:KAudioRommSeatFetherDefault])
    {
        return [[AudioRoomSeatsInfoFether alloc]init];
    }
    
    return nil;
}


+ (id<AudioRoomAuthProtocal>)getAuthorizationFether:(NSString *)type
{
    if ([type isEqualToString:KAudioRommAuthorizationFetherDefault])
    {
        return [[AudioRoomAuthorizationFether alloc]init];
    }
    
    return nil;
}
@end
