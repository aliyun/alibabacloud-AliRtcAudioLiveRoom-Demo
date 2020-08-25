//
//  AudioRoomOffLineController.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/7.
//

#import "AudioRoomOffLineController.h"
#import "NSBundle+RTCAudioLiveRoom.h"
#import <AFNetworking/AFNetworking.h>
#import "RTCHUD.h"

@interface AudioRoomOffLineController ()

@end

@implementation AudioRoomOffLineController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    UIImageView *bgView = [self.view viewWithTag:100];
    UIImageView *offLineView = [self.view viewWithTag:200];
    
    bgView.image = [NSBundle RALR_pngImageWithName:@"dark-background"];
    offLineView.image = [NSBundle RALR_pngImageWithName:@"offLine"];
}

- (IBAction)reConnection:(id)sender {
    if([AFNetworkReachabilityManager sharedManager].reachable){
         [self dismissViewControllerAnimated:YES completion:nil];
    }else{
         [RTCHUD showHud:@"网络连接失败,请检查网络" inView:self.view]; 
    }
}


@end
