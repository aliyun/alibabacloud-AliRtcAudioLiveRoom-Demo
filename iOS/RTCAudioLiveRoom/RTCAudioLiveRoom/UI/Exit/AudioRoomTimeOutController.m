//
//  AudioRoomTimeOutController.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/7.
//

#import "AudioRoomTimeOutController.h"

@interface AudioRoomTimeOutController ()

@end

@implementation AudioRoomTimeOutController

extern NSString *const Notification_ChatRoomLeaveChannel;

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}
- (IBAction)dismiss:(id)sender
{
    //离开频道
    UINavigationController *nav = (UINavigationController *)self.presentingViewController;
    [self dismissViewControllerAnimated:YES completion:^{
        [nav popViewControllerAnimated:YES];
    }]; 
}


@end
