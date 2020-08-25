//
//  NSBundle+VoiceCall.m
//  VoiceCall
//
//  Created by aliyun on 2020/4/21.
//

#import "NSBundle+VoiceCall.h"
#import "VoiceCallChannelViewController.h"

@implementation NSBundle (VoiceCall)

+ (instancetype)alivc_VoiceCallBundle {
    static NSBundle *VoiceCall = nil;
    if (VoiceCall == nil) {
        NSString *bundlePath = [[NSBundle bundleForClass:[VoiceCallChannelViewController class]] pathForResource:@"VoiceCall" ofType:@"bundle"];
        VoiceCall = [NSBundle bundleWithPath:bundlePath];
    }
    return VoiceCall;
}


+ (UIImage *)imageWithName:(NSString *)name type:(NSString *)type{
    int scale = [[UIScreen mainScreen] scale] <= 2 ? 2 : 3;
    NSString *fullName = [NSString stringWithFormat:@"%@@%dx",name,scale];
    NSString *path =  [[NSBundle alivc_VoiceCallBundle] pathForResource:fullName ofType:type];
    return [UIImage imageNamed:path];
}


+ (UIImage *)pngImageWithName:(NSString *)name {
    NSString *path =  [NSString stringWithFormat:@"VoiceCall.bundle/%@",name];
    return [UIImage imageNamed:path];
}

+ (NSString *)musicPathForResource:(NSString *)name {
    return [[NSBundle alivc_VoiceCallBundle] pathForResource:name ofType:@"mp3"];
}
@end
