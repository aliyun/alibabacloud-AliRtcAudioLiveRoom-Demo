//
//  AudioRoomMixSetting.h
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AudioRoomMixSetting : NSObject


/// 耳返
@property (assign, nonatomic) BOOL earBack;

/// 混响类型
@property (assign, nonatomic) NSInteger mixType;

/// 变声类型
@property (assign, nonatomic) NSInteger voiceChangeType;


- (void)resetData;
@end

NS_ASSUME_NONNULL_END
