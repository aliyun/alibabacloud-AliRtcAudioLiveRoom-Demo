//
//  UIViewController+Alert.h
//  VoiceCall
//
//  Created by aliyun on 2020/4/8.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIViewController (Alert)

/**
 @brief 提示框提醒

 @param message 提示信息
 */
- (void)showAlertWithMessage:(NSString *)message handler:(void (^)(UIAlertAction *action))handler;


- (void)showAlertWithMessage:(NSString *)message title:(NSString *)title actionTitle:(NSString *)actionTitle handler:(nonnull void (^)(UIAlertAction * _Nonnull))handler;

- (void)showMessage:(NSString *)message inView:(UIView *)view;

@end

NS_ASSUME_NONNULL_END
