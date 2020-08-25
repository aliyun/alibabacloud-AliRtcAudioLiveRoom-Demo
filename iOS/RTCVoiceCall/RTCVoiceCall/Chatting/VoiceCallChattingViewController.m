//
//  AlivcVoiceCallChattingViewController.m
//  VoiceCall
//
//  Created by aliyun on 2020/4/8.
//

#import "VoiceCallChattingViewController.h"
#import <AliRTCSdk/AliRTCSdk.h>
#import "UIViewController+Alert.h"
#import "VoiceCallSettingViewController.h"
#import "PresentTransition.h"
#import "PanInteractiveTransition.h"
#import "DismissTransition.h"
#import "NSBundle+VoiceCall.h"
#import "VideoCallUserAuthrization.h"
#import "UIViewController+Alert.h"
//#import "AFNetworkReachabilityManager.h"
#import <AFNetworking/AFNetworking.h>

typedef enum : NSUInteger {
    AlivcVoiceCallChattingRoomStateSingle, //房间有1人
    AlivcVoiceCallChattingRoomStateDouble, //房间有2人
} AlivcVoiceCallChattingRoomState;

#define ALIVC_MUSIC_NULL @"无音乐"
#define ALIVC_THEME_DEFAULT @"主题1"



@interface VoiceCallChattingViewController ()<AliRtcEngineDelegate,UIViewControllerTransitioningDelegate,AlivcModalViewControllerDelegate>

/**
@brief 房间号textfiled
*/
@property (copy, nonatomic) NSString *channelName;
/**
@brief 房间的人员状态
*/
@property (assign, nonatomic) AlivcVoiceCallChattingRoomState chattingRoomState;
/**
@brief appId
*/
@property (copy, nonatomic) NSString *appId;
/**
@brief userId
*/
@property (nonatomic,copy) NSString *userId;
/**
@brief 背景图片
*/
@property (weak,nonatomic) UIImageView *background;
/**
@brief 我的头像
*/
@property (weak,nonatomic) UIImageView *myAvatar;
/**
@brief 对方头像
*/
@property (weak,nonatomic) UIImageView *otherAvatar;
/**
@brief 对方标签
*/
@property (weak, nonatomic) UILabel *otherTagLabel;
/**
@brief 静音按钮
*/
@property (weak,nonatomic) UIButton *muteButton;
/**
@brief 扬声器按钮
*/
@property (weak,nonatomic) UIButton *speakerButton;
/**
@brief 挂断按钮
*/
@property (weak,nonatomic) UIButton *hangUpButton;
/**
@brief 房间创建时间
*/
@property (strong, nonatomic) NSDate *startDate;
/**
@brief 定时器
*/
@property (strong,nonatomic) NSTimer *timer;
/**
@brief 通话时间
*/
@property (weak, nonatomic) UILabel *timeLabel;
/**
@brief 等待标签
*/
@property (weak, nonatomic) UILabel *waitingLabel;
/**
@brief tipsView
*/
@property (weak, nonatomic) UIView *tipsView;
/**
@brief errorView
*/
@property (weak, nonatomic) UIView *errorView;

/**
@brief 当前推送到远端的声音id
*/
@property (nonatomic, assign) NSInteger currentSoundId;
/**
@brief 是否显示设置页面
*/
@property (nonatomic, assign) BOOL hasPresentSettingVC;
/**
@brief 对方网络是否较差
*/
@property (nonatomic, assign) BOOL otherNetworkIsPoor;
/**
@brief 自己网络是否较差
*/
@property (nonatomic, assign) BOOL selfNetworkIsPoor;
/**
@brief 当前主题
*/
@property (nonatomic, copy) NSString *currentTheme;
/**
@brief 显示的Transition
*/
@property (strong,nonatomic) PresentTransition *presentTransition;
/**
@brief 关闭的Transition
*/
@property (strong,nonatomic) DismissTransition *dismissTransition;
/**
@brief 回退手势的Transition
*/
@property (strong,nonatomic) PanInteractiveTransition *panInteractiveTransition;

/**
 @brief SDK实例
 */
@property (nonatomic, strong) AliRtcEngine *engine;

@end

@implementation VoiceCallChattingViewController

#pragma mark - init

/**
@brief 初始化方法
*/
- (instancetype)initWithChannelName:(NSString *)channelName appId:(NSString *)appId userId:(NSString *)userId {
    if(self = [super init]) {
        [self setSDK];
        self.channelName = channelName;
        self.appId = appId;
        self.userId = userId;
        [self initBaseData];
        
    }
    return self;
}

#pragma mark - life cycle
- (void)viewDidLoad {
    [super viewDidLoad];
    [self baseSetting];
    [self addSubviews];
    [self addNotification];
    [self roomInfo];
    [self createTimer];
    [self addNetworkingObserver];
    [self speakerButtonClicked:self.speakerButton];
    
}
- (void)viewWillAppear:(BOOL)animated {
    [self refreshRoomState:^(NSArray *users) {
        
    }];
}
- (void)dealloc {
    [self removeTimer];
}

#pragma mark - init data
- (void)initBaseData {
    [[NSUserDefaults standardUserDefaults] setObject:ALIVC_MUSIC_NULL forKey:ALIVC_SelectedMusicName];
    self.currentSoundId = 0;
    
    NSString *theme = [[NSUserDefaults standardUserDefaults] objectForKey:ALIVC_SelectedThemeName];
    if (theme.length == 0) {
        [[NSUserDefaults standardUserDefaults] setObject:ALIVC_THEME_DEFAULT forKey:ALIVC_SelectedThemeName];
        theme = ALIVC_THEME_DEFAULT;
    }
    self.currentTheme = theme;
    [[NSUserDefaults standardUserDefaults] synchronize];
    
}

- (void)setSDK {
    _engine = [AliRtcEngine sharedInstance:self extras:@""];
}


#pragma mark - timer

- (void)createTimer {
    self.timer = [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:@selector(updateTimeLabel) userInfo:nil repeats:YES];
}

- (void)removeTimer {
    [self.timer invalidate];
    self.timer = nil;
}

/**
@brief 更新计时label
*/
- (void)updateTimeLabel {
    if([[NSThread currentThread]isMainThread]){
        NSDate *currentDate= [[NSDate alloc] init];
        int diff = [currentDate timeIntervalSinceDate:self.startDate];
           if (diff > 600) {
               diff = 0;
           }
        self.timeLabel.text = [NSString stringWithFormat:@"%02d:%02d",diff/60,diff%60];
//        if (diff >= 600) {
//            if (self.hasPresentSettingVC) {
//                [self dismissViewControllerAnimated:YES completion:^{
//                    [self connectionTimeout];
//                }];
//            }else{
//                [self connectionTimeout];
//            }
//        }
    }
}

/**
@brief 体验超时
*/
- (void)connectionTimeout {
    [self removeTimer];
    if (!_engine) {
        return;
    }
    [self showAlertWithMessage:@"您的本次体验时长已满\n如需再次体验，请重新创建通话" title:@"体验时间已到" actionTitle:@"我知道了" handler:^(UIAlertAction * _Nonnull action) {
        [self leaveRoom];
    }];
}

#pragma arguments - network
/**
@brief 查询频道创建时间
*/
- (void)roomInfo {
    [VideoCallUserAuthrization channelStartTimeWithAppId:self.appId channel:self.channelName block:^(NSDate *startTime, NSError *error) {
        if (error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self showMessage:@"网络请求失败，请检查您的网络设置" inView:self.view];
            });
            return;
        }
        self.startDate = startTime ;
        [self.timer fire];
    }];
}

/**
@brief 刷新房间人数
*/
- (void)refreshRoomState:(void (^)(NSArray *users))block{
    [VideoCallUserAuthrization userListInChannel:self.channelName block:^(NSArray *users, NSError *error) {
        if (users.count == 2) {
            self.chattingRoomState = AlivcVoiceCallChattingRoomStateDouble;
        }else {
            self.chattingRoomState = AlivcVoiceCallChattingRoomStateSingle;
        }
        block(users);
    }];
}



#pragma mark - notification
- (void)addNotification {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(refreshThemeAndMusic) name:ALIVC_RefreshBackgroudAndMusic object:nil];
    
    [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(leaveChannel) name:ALIVC_ApplicationWillTerminate object:nil];
}
/**
@brief 更新背景和伴奏
*/
- (void)refreshThemeAndMusic {
    self.hasPresentSettingVC = NO;
    //  恢复背景
    NSString *theme = [[NSUserDefaults standardUserDefaults] objectForKey:ALIVC_SelectedThemeName];
    self.currentTheme = theme;
    self.background.image = [NSBundle pngImageWithName:theme];
    
    NSString *music = [[NSUserDefaults standardUserDefaults] objectForKey:ALIVC_SelectedMusicName];
    NSString *musicId = [[NSUserDefaults standardUserDefaults] objectForKey:ALIVC_SelectedMusicId];
    //停止正在播放的音乐
    [self stopLocalMusic:self.currentSoundId];
    //正式播放的音乐id = 设置页面音乐id + 100
    self.currentSoundId = [musicId integerValue] + 100;
    //播放当前音乐
    [self playLoaclMusic:music soundId:_currentSoundId];
}
/**
@brief 监听网络变化
*/
- (void)addNetworkingObserver {
    [[AFNetworkReachabilityManager sharedManager] setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
        if (status == AFNetworkReachabilityStatusNotReachable) {
            self.errorView.hidden = NO;
        } else {
            self.errorView.hidden = YES;
        }
    }];
    
    [[AFNetworkReachabilityManager sharedManager] startMonitoring];
}

#pragma mark - sdk operation

#pragma mark - backgroud music operation
/**
@brief 播放伴奏
*/
- (void)playLoaclMusic:(NSString *)musicName soundId:(NSInteger)soundId {
    NSString *path = [NSBundle musicPathForResource:musicName];
    if(![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        return;
    }
    NSString *url = [path stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    [self.engine preloadAudioEffectWithSoundId:soundId filePath:url];
    [self.engine playAudioEffectWithSoundId:soundId filePath:url cycles:1 publish:YES];
}

/**
@brief 停止伴奏
*/
- (void)stopLocalMusic:(NSInteger)soundId {
    [self.engine stopAudioEffectWithSoundId:soundId];
}

/**
@brief 离开频道
*/
- (void)leaveChannel {
    if (_engine) {
        //离开频道
        [self.engine leaveChannel];
        //销毁SDK实例
        [AliRtcEngine destroy];
        
        _engine = nil;
    }
}


#pragma mark - actions
/**
@brief 跳转设置界面
*/
- (void)settting {
    self.transitioningDelegate = self;
    VoiceCallSettingViewController *vc = [[VoiceCallSettingViewController alloc] init];
    vc.modalPresentationStyle = UIModalPresentationCustom;
    vc.transitioningDelegate = self;
    vc.engine = self.engine;
    vc.delegate  = self;
    [self.panInteractiveTransition wireToViewController:vc];
    [self presentViewController:vc animated:YES completion:^{
        self.hasPresentSettingVC = YES;
    }];
}

/**
@brief 手动关闭聊天界面
*/
- (void)close {
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"退出语聊"
                                                                             message:@"您的本次体验时长未满可继续体验"
                                                                      preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *confirm = [UIAlertAction actionWithTitle:@"确认退出" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        [self leaveRoom];
    }];
    
    [confirm setValue:[UIColor colorWithRed:155/255.0 green:155/255.0 blue:155/255.0 alpha:1/1.0] forKey:@"titleTextColor"];
    
    UIAlertAction *cancel = [UIAlertAction actionWithTitle:@"继续体验" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        
    }];
    
    [alertController addAction:confirm];
    [alertController addAction:cancel];
    [self.navigationController presentViewController:alertController animated:YES completion:nil];
}

/**
@brief 静音
*/
- (void)muteButtonClicked:(UIButton *)button {
    int result = [self.engine muteLocalMic:!button.selected];
    if (result == 0) {
        button.selected = !button.selected;
        if (button.selected) {
            [self showMessage:@"静音模式已开启" inView:self.view];
        }else{
            [self showMessage:@"静音模式已取消" inView:self.view];
        }
    }
}

/**
@brief 离开聊天页面
*/
- (void)leaveRoom{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self dismissViewControllerAnimated:YES completion:^{
            [UIApplication sharedApplication].idleTimerDisabled = NO;
            [self leaveChannel];
        }];
    });
}

/**
@brief 开启/关闭扬声器
*/
- (void)speakerButtonClicked:(UIButton *)button {
    int result = [self.engine enableSpeakerphone:!button.selected];
    if (result == 0) {
        button.selected = !button.selected;
        if (button.selected) {
            [self showMessage:@"扬声器已开启" inView:self.view];
        }else{
            [self showMessage:@"扬声器已取消" inView:self.view];
        }
    }
}
/**
@brief 关闭提示banner
*/
- (void)closeTips {
    self.tipsView.hidden = YES;
}

#pragma mark  - AlivcModalViewControllerDelegate

-(id<UIViewControllerAnimatedTransitioning>)animationControllerForPresentedController:(UIViewController *)presented presentingController:(UIViewController *)presenting sourceController:(UIViewController *)source{
    return self.presentTransition;
}

- (nullable id <UIViewControllerAnimatedTransitioning>)animationControllerForDismissedController:(UIViewController *)dismissed {
    return self.dismissTransition;
}

- (id <UIViewControllerInteractiveTransitioning>)interactionControllerForDismissal:(id <UIViewControllerAnimatedTransitioning>)animator{
    return self.panInteractiveTransition.interacting ? self.panInteractiveTransition : nil;
}

#pragma mark - AlivcModalViewControllerDelegate

- (void)dismissModalViewController:(UIViewController *)viewController {
    [self dismissViewControllerAnimated:YES completion:^{
        self.hasPresentSettingVC = NO;
    }];
}


#pragma mark - AliRtcEngineDelegate

- (void)onRemoteUserOnLineNotify:(NSString *)uid {
    NSLog(@"远端用户上线");
    self.chattingRoomState = AlivcVoiceCallChattingRoomStateDouble;
}

- (void)onRemoteUserOffLineNotify:(NSString *)uid {
    NSLog(@"远端用户下线");
    self.chattingRoomState = AlivcVoiceCallChattingRoomStateSingle;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self showMessage:@"对方已退出房间" inView:self.view];
    });
}

- (void)onConnectionLost {
    NSLog(@"链接断开");
    
}

- (void)onConnectionRecovery {
    NSLog(@"链接恢复");
    
}

- (void)onTryToReconnect {
    NSLog(@"链接重连");
}
- (void)onBye:(int)code {
    NSLog(@"服务器提出频道");
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.hasPresentSettingVC) {
            [self dismissViewControllerAnimated:YES completion:^{
                [self connectionTimeout];
            }];
        }else{
            [self connectionTimeout];
        }
    });
}

- (void)onOccurWarning:(int)warn {
    NSLog(@"warning");
}

- (void)onUserAudioInterruptedBegin:(NSString *)uid {
    NSLog(@"用户被中断");
}

- (void)onUserAudioInterruptedEnded:(NSString *)uid {
    NSLog(@"用户中断结束");
}

/**
@brief 用户的网络状态变化
*/
- (void)onNetworkQualityChanged:(NSString *)uid
               upNetworkQuality:(AliRtcNetworkQuality)upQuality
             downNetworkQuality:(AliRtcNetworkQuality)downQuality {
    
    if([uid isEqualToString:@""] || [uid isEqualToString:self.userId]) {
        //自己的网络状态
        if (upQuality >= AlivcRtcNetworkQualityBad) {
            if (!self.selfNetworkIsPoor) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self showMessage:@"当前网络不佳" inView:self.view];
                });
                self.selfNetworkIsPoor = YES;
            }
        }else{
            self.selfNetworkIsPoor = NO;
        }
    } else {
        //对方的网络状态
        if (upQuality >= AlivcRtcNetworkQualityBad) {
            if (!self.otherNetworkIsPoor) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self showMessage:@"对方网络不佳" inView:self.view];
                });
                self.otherNetworkIsPoor = YES;
            }
        } else {
            self.otherNetworkIsPoor = NO;
        } 
    }
    
}

/**
@brief 发生错误回调
*/
- (void)onOccurError:(int)error {
    [self showAlertWithMessage:@"系统错误，请点击确定退出" handler:^(UIAlertAction * _Nonnull action) {
        [self leaveRoom];
    }];
}

#pragma mark - UI
- (void)baseSetting {
    self.title = [NSString stringWithFormat:@"房间号:%@",self.channelName];
    self.view.backgroundColor = [UIColor blackColor];
    [self.navigationController.navigationBar setBackgroundImage:[UIImage new] forBarMetrics:0];
    self.navigationController.navigationBar.layer.masksToBounds = YES;
    //标题颜色
    [[UINavigationBar appearance] setTitleTextAttributes:@{NSForegroundColorAttributeName:[UIColor whiteColor]}];
    
    //设置导航栏
    UIButton *settingBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    settingBtn.bounds = CGRectMake(0, 0, 30, 44);
    [settingBtn setImage:[NSBundle pngImageWithName:@"设置"] forState:UIControlStateNormal];
    [settingBtn addTarget:self action:@selector(settting) forControlEvents:UIControlEventTouchUpInside];
    UIBarButtonItem *setting =  [[UIBarButtonItem alloc] initWithCustomView:settingBtn];
    self.navigationItem.rightBarButtonItem = setting;
    
    //返回按钮
    UIButton *closeBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    closeBtn.bounds = CGRectMake(0, 0, 30, 44);
    [closeBtn setImage:[NSBundle pngImageWithName:@"angle_left"] forState:UIControlStateNormal];
    [closeBtn addTarget:self action:@selector(close) forControlEvents:UIControlEventTouchUpInside];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:closeBtn];
    
    //防止锁屏
    [UIApplication sharedApplication].idleTimerDisabled = YES;
}

- (void)addSubviews {
    
    //0 theme
    UIImageView *background = [[UIImageView alloc] initWithFrame:self.view.bounds];
    background.image = [NSBundle pngImageWithName:self.currentTheme];
    background.contentMode = UIViewContentModeScaleAspectFill;
    [self.view addSubview:background];
    self.background = background;
    //1.avartor
    CGSize screenSize = self.view.bounds.size;
    CGFloat width = 96;
    CGFloat height = 96;
    CGFloat x = screenSize.width / 4.0 - width * 0.5 ;
    CGFloat y = 160;
    
    UIImageView *myAvatar = [[UIImageView alloc] initWithImage:nil];
    myAvatar.frame = CGRectMake(x, y, width, height);
    myAvatar.layer.cornerRadius = 48;
    myAvatar.layer.masksToBounds = YES;
    [self.view addSubview:myAvatar];
    myAvatar.image = [NSBundle pngImageWithName:@"男头1"];
    
    //mineTag
    CGFloat myTagY = CGRectGetMaxY(myAvatar.frame) + 20;
    CGFloat myTagH = 30;
    UILabel *myTag = [[UILabel alloc] initWithFrame:CGRectMake(x, myTagY, width, myTagH)];
    myTag.text = @"我";
    myTag.textAlignment = NSTextAlignmentCenter;
    myTag.backgroundColor = [UIColor colorWithRed:92/255.0 green:205/255.0 blue:232/255.0 alpha:1];
    myTag.layer.cornerRadius =  myTagH * 0.5;
    myTag.layer.masksToBounds = YES;
    myTag.textColor = [UIColor whiteColor];
    myTag.font = [UIFont fontWithName:@"PingFangSC-Regular" size:16];
    [self.view addSubview:myTag];
    
    CGFloat x2 = screenSize.width - width - x;
    UIImageView *otherAvatar = [[UIImageView alloc] initWithImage:nil];
    otherAvatar.frame = CGRectMake(x2, y, width, height);
    otherAvatar.layer.cornerRadius = 48;
    otherAvatar.layer.masksToBounds = YES;
    [self.view addSubview:otherAvatar];
    otherAvatar.image = [NSBundle pngImageWithName:@"男头2"];
    
    CGFloat otherTagY = CGRectGetMaxY(otherAvatar.frame) + 20;
    CGFloat otherTagX = x2;
    UILabel *otherTag = [[UILabel alloc] initWithFrame:CGRectMake(otherTagX, otherTagY, width, myTagH)];
    otherTag.text = @"虚位以待";
    otherTag.textAlignment = NSTextAlignmentCenter;
    otherTag.textColor = [UIColor whiteColor];
    otherTag.font = [UIFont fontWithName:@"PingFangSC-Regular" size:16];
    [self.view addSubview:otherTag];
    self.otherTagLabel = otherTag;
    
    self.myAvatar = myAvatar;
    self.otherAvatar = otherAvatar;
    
    myAvatar.backgroundColor = [UIColor redColor];
    otherAvatar.backgroundColor = [UIColor redColor];
    
    //2.buttons
    CGFloat buttonW = 68;
    CGFloat buttonH = 68;
    CGFloat buttonX = screenSize.width/6.0 - buttonW*0.5;
    CGFloat buttonY = screenSize.height - buttonH - 40;
    
    UIButton *muteButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [muteButton setImage:[NSBundle pngImageWithName:@"非静音"] forState:UIControlStateNormal];
    [muteButton setImage:[NSBundle pngImageWithName:@"静音"] forState:UIControlStateSelected];
    muteButton.frame = CGRectMake(buttonX, buttonY, buttonW, buttonH);
    [self.view addSubview:muteButton];
    self.muteButton = muteButton;
    
    CGFloat labelY = CGRectGetMaxY(muteButton.frame) + 10;
    CGFloat labelH = 24;
    UILabel *muteLabel = [[UILabel alloc] initWithFrame:CGRectMake(buttonX, labelY, buttonW, labelH)];
    muteLabel.text = @"静音";
    muteLabel.textColor = [UIColor colorWithRed:255/255.0 green:255/255.0 blue:255/255.0 alpha:1/1.0];
    muteLabel.font =  [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    muteLabel.textAlignment = NSTextAlignmentCenter;
    [self.view addSubview:muteLabel];
    
    
    CGFloat buttonX2 = (screenSize.width - buttonW) *0.5;
    UIButton *hangUpButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [hangUpButton setImage:[NSBundle pngImageWithName:@"挂断"] forState:UIControlStateNormal];
    hangUpButton.frame = CGRectMake(buttonX2 , buttonY, buttonW, buttonH);
    [self.view addSubview:hangUpButton];
    self.hangUpButton = hangUpButton;
    
    UILabel *hangUpLabel = [[UILabel alloc] initWithFrame:CGRectMake(buttonX2, labelY, buttonW, labelH)];
    hangUpLabel.text = @"挂断";
    hangUpLabel.textColor = [UIColor colorWithRed:255/255.0 green:255/255.0 blue:255/255.0 alpha:1/1.0];
    hangUpLabel.font =  [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    hangUpLabel.textAlignment = NSTextAlignmentCenter;
    [self.view addSubview:hangUpLabel];
    
    
    
    CGFloat buttonX3 = screenSize.width - buttonW - buttonX;
    UIButton *speakerButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [speakerButton setImage:[NSBundle pngImageWithName:@"免提关闭"] forState:UIControlStateNormal];
    [speakerButton setImage:[NSBundle pngImageWithName:@"免提开启"] forState:UIControlStateSelected];
    speakerButton.frame = CGRectMake(buttonX3, buttonY, buttonW, buttonH);
    [self.view addSubview:speakerButton];
    self.speakerButton = speakerButton;
    
    UILabel *speakerLabel = [[UILabel alloc] initWithFrame:CGRectMake(buttonX3, labelY, buttonW, labelH)];
    speakerLabel.text = @"免提";
    speakerLabel.textColor = [UIColor colorWithRed:255/255.0 green:255/255.0 blue:255/255.0 alpha:1/1.0];
    speakerLabel.font =  [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    speakerLabel.textAlignment = NSTextAlignmentCenter;
    [self.view addSubview:speakerLabel];
    
    //add action
    [self.muteButton addTarget:self action:@selector(muteButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    
    [self.hangUpButton addTarget:self action:@selector(leaveRoom) forControlEvents:UIControlEventTouchUpInside];
    
    [self.speakerButton addTarget:self action:@selector(speakerButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    
    //tips
    UIView *tipsView = [[UIView alloc] init];
    CGFloat tipsH = 44;
    CGFloat tipsW = screenSize.width;
    
    CGFloat tipsY = self.navigationController.navigationBar.bounds.size.height + [UIApplication sharedApplication].statusBarFrame.size.height ;
    tipsView.frame = CGRectMake(0, tipsY, tipsW, tipsH);
    tipsView.backgroundColor = [UIColor colorWithRed:80/255.0 green:85/255.0 blue:98/255.0 alpha:1/1.0];
    [self.view addSubview:tipsView];
    self.tipsView = tipsView;
    
    UILabel *tipsLabel = [[UILabel alloc] initWithFrame:CGRectMake(18, 0, tipsW, tipsH)];
    tipsLabel.text = @"您的欢乐体验时长为10分钟";
    tipsLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
    tipsLabel.textColor = [UIColor colorWithRed:255/255.0 green:184/255.0 blue:66/255.0 alpha:1/1.0];
    [tipsView addSubview:tipsLabel];
    
    UIButton *tipsCloseBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    tipsCloseBtn.frame = CGRectMake(tipsW-tipsH, 0, tipsH, tipsH);
    [tipsCloseBtn setImage:[NSBundle pngImageWithName:@"关闭"] forState:UIControlStateNormal];
    [tipsView addSubview:tipsCloseBtn];
    [tipsCloseBtn addTarget:self action:@selector(closeTips) forControlEvents:UIControlEventTouchUpInside];
    
    
    //network error
    UIView *errorView = [[UIView alloc] init];
    CGFloat errorH = 44;
    CGFloat errorW = screenSize.width;
    
    CGFloat errorY = self.navigationController.navigationBar.bounds.size.height + [UIApplication sharedApplication].statusBarFrame.size.height ;
    errorView.frame = CGRectMake(0, errorY, errorW, errorH);
    errorView.backgroundColor = [UIColor colorWithRed:80/255.0 green:85/255.0 blue:98/255.0 alpha:1/1.0];
    [self.view addSubview:errorView];
    self.errorView = errorView;
    
    UILabel *errorLabel = [[UILabel alloc] initWithFrame:CGRectMake(18, 0, tipsW, tipsH)];
    errorLabel.text = @"网络连接失败,请检查你的网络";
    errorLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
    errorLabel.textColor = [UIColor colorWithRed:252/255.0 green:67/255.0 blue:71/255.0 alpha:1/1.0];
    [errorView addSubview:errorLabel];
    errorView.hidden = YES;

    //time
    UILabel *time = [[UILabel alloc] init];
    time.text = @"00:00";
    time.font = [UIFont fontWithName:@"AlibabaSans102-Bold" size:20];
    time.textColor = [UIColor colorWithRed:255/255.0 green:255/255.0 blue:255/255.0 alpha:1/1.0];
//    [time sizeToFit];
    time.bounds = CGRectMake(0, 0, 80, 24);
    time.center = CGPointMake(screenSize.width * 0.5, buttonY - 50);
    time.backgroundColor = [UIColor colorWithRed:23/255.0 green:23/255.0 blue:23/255.0 alpha:1];
    time.textAlignment = NSTextAlignmentCenter;
    time.layer.cornerRadius = 12;
    time.layer.masksToBounds = YES;
    [self.view addSubview:time];
    self.timeLabel = time;
    
    UILabel *waiting = [[UILabel alloc] init];
    waiting.text = @"请静候对方进入";
    waiting.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
    waiting.textColor = [UIColor colorWithRed:255/255.0 green:255/255.0 blue:255/255.0 alpha:1/1.0];
    waiting.backgroundColor = [UIColor colorWithRed:23/255.0 green:23/255.0 blue:23/255.0 alpha:1];
    waiting.textAlignment = NSTextAlignmentCenter;
    waiting.bounds = CGRectMake(0, 0, 120, 24);
    waiting.layer.cornerRadius = 12;
    waiting.layer.masksToBounds = YES;
    waiting.center = CGPointMake(screenSize.width * 0.5, buttonY - 50);
    [self.view addSubview:waiting];
    self.waitingLabel = waiting;
    
    //刷新UI
    self.chattingRoomState = _chattingRoomState;
}
#pragma mark - getter

- (PresentTransition *)presentTransition {
    if (!_presentTransition) {
        _presentTransition = [[PresentTransition alloc] init];
    }
    return _presentTransition;;
}

- (DismissTransition *)dismissTransition {
    if (!_dismissTransition) {
        _dismissTransition = [[DismissTransition alloc] init];
    }
    return _dismissTransition;
}

- (PanInteractiveTransition *)panInteractiveTransition {
    if (!_panInteractiveTransition) {
        _panInteractiveTransition = [[PanInteractiveTransition alloc] init];
    }
    return _panInteractiveTransition;
}

- (void)setChattingRoomState:(AlivcVoiceCallChattingRoomState)chattingRoomState {
    _chattingRoomState = chattingRoomState;
    dispatch_async(dispatch_get_main_queue(), ^{
        if (chattingRoomState == AlivcVoiceCallChattingRoomStateSingle) {
            self.timeLabel.hidden = YES;
            self.otherTagLabel.hidden = NO;
            self.otherAvatar.image = [NSBundle pngImageWithName:@"男头2灰色"];
            self.waitingLabel.hidden = NO;
        } else {
            //double
            self.timeLabel.hidden = NO;
            self.otherTagLabel.hidden = YES;
            self.otherAvatar.image = [NSBundle pngImageWithName:@"男头2"];
            self.waitingLabel.hidden = YES;
        }
    });
}
@end
