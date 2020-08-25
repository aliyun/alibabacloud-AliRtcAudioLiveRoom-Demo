//
//  NSBundle+RTCAudioLiveRoom.h
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/6/30.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSBundle (RTCAudioLiveRoom)

+ (instancetype)RTC_AudioLiveRoomBundle;

+ (UIImage *)RALR_imageWithName:(NSString *)name type:(NSString *)type;

+ (UIImage *)RALR_pngImageWithName:(NSString *)name;
 
+ (UIStoryboard *)RALR_storyboard;

@end

NS_ASSUME_NONNULL_END
