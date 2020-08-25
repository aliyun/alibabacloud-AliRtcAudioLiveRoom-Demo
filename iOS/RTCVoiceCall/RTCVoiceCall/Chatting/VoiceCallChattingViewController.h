//
//  AlivcVoiceCallChattingViewController.h
//  VoiceCall
//
//  Created by aliyun on 2020/4/8.
//

#import <UIKit/UIKit.h>



#define ALIVC_ApplicationWillTerminate @"ALIVC_ApplicationWillTerminate"

NS_ASSUME_NONNULL_BEGIN

@interface VoiceCallChattingViewController : UIViewController


/// 初始化方法
/// @param channelName 频道名称
/// @param appId 应用id
/// @param userId 用户id
- (instancetype)initWithChannelName:(NSString *)channelName appId:(NSString *)appId userId:(NSString *)userId;

@end

NS_ASSUME_NONNULL_END
