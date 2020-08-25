//
//  AudioRoomSoundEffect.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/13.
//

#import "AudioRoomSoundEffect.h"
#import "NSBundle+RTCAudioLiveRoom.h"

@implementation AudioRoomSoundEffect

- (instancetype)initWithEffectId:(NSInteger)effectId fileName:(NSString *)fileName
{
    if (self = [super init]) {
        self.effectId = effectId;
        self.path =[[NSBundle RTC_AudioLiveRoomBundle] pathForResource:fileName ofType:nil];
        self.volume = 100;
        self.testing = NO;
        self.publishing = NO;
    }
    
    return self;
}

- (void)resetData
{
    self.testing = NO;
    self.publishing = NO;
}
@end
