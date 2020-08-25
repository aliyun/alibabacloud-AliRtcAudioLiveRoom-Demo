//
//  NSBundle+RTCAudioLiveRoom.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/6/30.
//

#import "NSBundle+RTCAudioLiveRoom.h"
#import "AudioRoomLoginController.h"

@implementation NSBundle (RTCAudioLiveRoom)

+ (instancetype)RTC_AudioLiveRoomBundle
{
    static NSBundle *bundel = nil;
    if (bundel == nil) {
        NSString *bundlePath = [[NSBundle bundleForClass:[AudioRoomLoginController class]] pathForResource:@"RTCAudioLiveRoom" ofType:@"bundle"];
        bundel = [NSBundle bundleWithPath:bundlePath];
    }
    return bundel;
}

+ (UIImage *)RALR_imageWithName:(NSString *)name type:(NSString *)type
{
    int scale = [[UIScreen mainScreen] scale] <= 2 ? 2 : 3;
    NSString *fullName = [NSString stringWithFormat:@"%@@%dx",name,scale];
    NSString *path =  [[NSBundle RTC_AudioLiveRoomBundle] pathForResource:fullName ofType:type];
    return [UIImage imageNamed:path];
}

+ (UIImage *)RALR_pngImageWithName:(NSString *)name
{
    NSString *path =  [NSString stringWithFormat:@"RTCAudioLiveRoom.bundle/%@",name];
    return [UIImage imageNamed:path];
}

+ (UIStoryboard *)RALR_storyboard
{
    return [UIStoryboard storyboardWithName:@"RTCAudioLiveRoom"
                                     bundle:[NSBundle bundleForClass:[AudioRoomLoginController class]]];
}


@end
