//
//  AudioLiveRoomLoginController.h
//  AFNetworking
//
//  Created by Aliyun on 2020/6/30.
//

#import <UIKit/UIKit.h>
#import <AliRTCSdk/AliRTCSdk.h>
 
NS_ASSUME_NONNULL_BEGIN

@interface AudioRoomChatController : UIViewController

- (instancetype)initWithChanel:(NSString *)channel
                          role:(AliRtcClientRole)role;

@property (nonatomic,copy) NSString *channel;

@property (assign,nonatomic) AliRtcClientRole currentRole;

@end

NS_ASSUME_NONNULL_END
