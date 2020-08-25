//
//  AudioLiveRoomExitController.h
//  Pods
//
//  Created by Aliyun on 2020/7/7.
//

#import <UIKit/UIKit.h>
 

NS_ASSUME_NONNULL_BEGIN

@interface AudioRoomExitController : UIViewController

@property (copy,nonatomic) void(^complete)(int result);

@end

NS_ASSUME_NONNULL_END
