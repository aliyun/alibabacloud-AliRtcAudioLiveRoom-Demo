//
//  AudioRoomMixSetting.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/13.
//

#import "AudioRoomMixSetting.h"

@implementation AudioRoomMixSetting

- (instancetype)init
{
    if (self = [super init])
    {
        self.mixType = 0;
        self.earBack = NO;
    }
    
    return self;
}

- (void)resetData
{
    self.mixType = 0;
    self.earBack = NO;
    self.voiceChangeType = 0;
}

@end
