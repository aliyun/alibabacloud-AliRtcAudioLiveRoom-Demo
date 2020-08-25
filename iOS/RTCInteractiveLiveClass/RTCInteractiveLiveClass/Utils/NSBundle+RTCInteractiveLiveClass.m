//
//  NSBundle+RTCInteractiveLiveClass.m
//  RTCInteractiveLiveClass
//
//  Created by Aliyun on 2020/6/28.
//

#import "NSBundle+RTCInteractiveLiveClass.h"
#import "LoginController.h"

@implementation NSBundle (RTCInteractiveLiveClass)

+ (instancetype)RTC_InteractiveLiveClassBundle{
    static NSBundle *VoiceCall = nil;
    if (VoiceCall == nil) {
        NSString *bundlePath = [[NSBundle bundleForClass:[LoginController class]] pathForResource:@"RTCInteractiveLiveClass" ofType:@"bundle"];
        VoiceCall = [NSBundle bundleWithPath:bundlePath];
    }
    return VoiceCall;
}

+ (UIImage *)RILC_imageWithName:(NSString *)name type:(NSString *)type{
    int scale = [[UIScreen mainScreen] scale] <= 2 ? 2 : 3;
    NSString *fullName = [NSString stringWithFormat:@"%@@%dx",name,scale];
    NSString *path =  [[NSBundle RTC_InteractiveLiveClassBundle] pathForResource:fullName ofType:type];
    return [UIImage imageNamed:path];
}

+ (UIImage *)RILC_pngImageWithName:(NSString *)name{
    NSString *path =  [NSString stringWithFormat:@"RTCInteractiveLiveClass.bundle/%@",name];
    return [UIImage imageNamed:path];
}


+ (UIStoryboard *)RILC_storyboard {
    return [UIStoryboard storyboardWithName:@"RTCInteractiveLiveClass" bundle:[NSBundle bundleForClass:[LoginController class]]];
}

@end
