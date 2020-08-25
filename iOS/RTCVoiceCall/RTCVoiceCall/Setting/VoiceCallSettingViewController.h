//
//  AlivcVoiceCallSettingViewController.h
//  VoiceCall
//
//  Created by aliyun on 2020/4/10.
//

#import <UIKit/UIKit.h>
#import <AliRTCSdk/AliRTCSdk.h>
NS_ASSUME_NONNULL_BEGIN

#define ALIVC_SelectedThemeName  @"ALIVC_SelectedThemeName"

#define ALIVC_SelectedMusicName  @"ALIVC_SelectedMusicName"

#define ALIVC_SelectedMusicId  @"ALIVC_SelectedMusicId"

#define ALIVC_RefreshBackgroudAndMusic  @"ALIVC_RefreshBackgroudAndMusic"


@protocol AlivcModalViewControllerDelegate <NSObject>

/// dismiss controller 的代理方法
/// @param viewController 当前控制器
-(void)dismissModalViewController:(UIViewController *)viewController;

@end

@interface VoiceCallSettingViewController : UIViewController

/**
@brief dismiss控制器的代理
*/

@property (weak,nonatomic) id<AlivcModalViewControllerDelegate> delegate;
/**
 @brief SDK实例
 */
@property (nonatomic, strong) AliRtcEngine *engine;


@end

NS_ASSUME_NONNULL_END
