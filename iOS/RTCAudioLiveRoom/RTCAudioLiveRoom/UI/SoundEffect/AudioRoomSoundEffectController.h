//
//  SoundEffectController.h
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/6.
//

#import <UIKit/UIKit.h>
#import "AudioRoomSoundEffect.h"

NS_ASSUME_NONNULL_BEGIN

@interface AudioRoomSoundEffectController : UIViewController

- (instancetype)initWithEffect1:(AudioRoomSoundEffect *)effect1
                        effect2:(AudioRoomSoundEffect *)effect2;
@end

NS_ASSUME_NONNULL_END
