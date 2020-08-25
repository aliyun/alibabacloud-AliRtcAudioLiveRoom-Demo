//
//  AudioRoomSoundEffect.h
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AudioRoomSoundEffect : NSObject

/// 音效Id
@property (assign, nonatomic) NSInteger effectId;

/// 音量
@property (assign, nonatomic) NSInteger volume;

/// 路径
@property (copy, nonatomic) NSString *path;

/// 测试播放中
@property (assign, nonatomic) BOOL testing;

/// 推送中
@property (assign, nonatomic) BOOL publishing;

- (instancetype)initWithEffectId:(NSInteger)effectId fileName:(NSString *)fileName;

- (void)resetData;

@end

NS_ASSUME_NONNULL_END
