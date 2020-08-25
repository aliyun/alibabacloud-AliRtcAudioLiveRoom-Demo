//
//  AudioRoomBackgroundMusic.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/13.
//

#import "AudioRoomBackgroundMusic.h"
#import "NSBundle+RTCAudioLiveRoom.h"

@implementation AudioRoomBackgroundMusic

- (instancetype)init {
    if (self = [super init])
    {
        NSString *path = [[NSBundle RTC_AudioLiveRoomBundle]
                          pathForResource:@"Yippee.mp3"
                          ofType:nil];
        
        self.volume = 100;
        self.path =path;
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
