//
//  MixController.h
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/6.
//

#import <UIKit/UIKit.h>
#import "AudioRoomMixSetting.h"
NS_ASSUME_NONNULL_BEGIN

@interface AudioRoomMixController : UIViewController

- (instancetype)initWithMixSetting:(AudioRoomMixSetting *)mixSetting;

@end

NS_ASSUME_NONNULL_END
