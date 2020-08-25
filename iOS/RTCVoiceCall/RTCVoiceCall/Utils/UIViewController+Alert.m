//
//  UIViewController+Alert.m
//  VoiceCall
//
//  Created by aliyun on 2020/4/8.
//

#import "UIViewController+Alert.h"
#import "MBProgressHUD.h"

@implementation UIViewController (Alert)

- (void)showAlertWithMessage:(NSString *)message handler:(nonnull void (^)(UIAlertAction * _Nonnull))handler{
    dispatch_async(dispatch_get_main_queue(), ^{
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"提示"
                                                                                 message:message
                                                                          preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *confirm = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            if (handler) {
                handler(action);
            }
        }];
        [alertController addAction:confirm];
        [self.navigationController presentViewController:alertController animated:YES completion:nil];
    });
    
}

- (void)showAlertWithMessage:(NSString *)message title:(NSString *)title actionTitle:(NSString *)actionTitle handler:(nonnull void (^)(UIAlertAction * _Nonnull))handler{
    dispatch_async(dispatch_get_main_queue(), ^{
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:title
                                                                                 message:message
                                                                          preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *confirm = [UIAlertAction actionWithTitle:actionTitle style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            if (handler) {
                handler(action);
            }
        }];
        [alertController addAction:confirm];
        [self.navigationController presentViewController:alertController animated:YES completion:nil];
    });
    
}

- (void)showMessage:(NSString *)message inView:(UIView *)view{
    if (view) {
        MBProgressHUD  *hud =[MBProgressHUD showHUDAddedTo:view animated:true];
        hud.mode = MBProgressHUDModeText;
        hud.bezelView.style = MBProgressHUDBackgroundStyleSolidColor;
        hud.bezelView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.8];
        hud.contentColor = [UIColor whiteColor];
        hud.label.numberOfLines = 10;
        hud.label.text = message;
        [hud hideAnimated:true afterDelay:1];
        hud.userInteractionEnabled = NO;
    }
}

@end
