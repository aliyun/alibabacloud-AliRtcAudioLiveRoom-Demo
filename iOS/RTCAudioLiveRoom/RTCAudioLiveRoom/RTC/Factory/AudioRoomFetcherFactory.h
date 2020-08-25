//
//  AudioRoomSeatsLoaderFactory.h
//  Pods
//
//  Created by Aliyun on 2020/7/22.
//

#import <Foundation/Foundation.h>
#import "AudioRoomSeatsInfoProtocal.h"
#import "AudioRoomAuthProtocal.h"

#define KAudioRommSeatFetherDefault @"AudioRommSeatFethererDefault"
#define KAudioRommAuthorizationFetherDefault @"AudioRommAuthorizationFetherDefault"

NS_ASSUME_NONNULL_BEGIN

@interface AudioRoomFetcherFactory : NSObject

+ (id<AudioRoomSeatsInfoProtocal>)getSeatsInfoFetcher:(NSString *)type;

+ (id<AudioRoomAuthProtocal>)getAuthorizationFether:(NSString *)type;

@end

NS_ASSUME_NONNULL_END
