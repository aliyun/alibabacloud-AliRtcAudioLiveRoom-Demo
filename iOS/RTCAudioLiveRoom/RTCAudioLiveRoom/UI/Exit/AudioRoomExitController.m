//
//  AudioLiveRoomExitController.m
//  Pods
//
//  Created by Aliyun on 2020/7/7.
//

#import "AudioRoomExitController.h"


@interface AudioRoomExitController ()

@end

@implementation AudioRoomExitController

- (void)viewDidLoad {
    [super viewDidLoad];
}
- (IBAction)leaveChannel:(id)sender
{
    //离开频道
    __weak typeof(self) weakSelf = self;
    [self dismissViewControllerAnimated:YES completion:^{
        if (weakSelf.complete) {
            weakSelf.complete(1);
        }
    }];
}

- (IBAction)dissmiss:(id)sender
{
    __weak typeof(self) weakSelf = self;
    [self dismissViewControllerAnimated:YES completion:^{
        if (weakSelf.complete) {
            weakSelf.complete(0);
        }
    }];
}

@end
