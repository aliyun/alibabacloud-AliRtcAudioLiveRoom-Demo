//
//  AudioLiveRoomLoginController.m
//  AFNetworking
//
//  Created by Aliyun on 2020/6/30.
//

#import "AudioRoomChatController.h"
#import "RTCCommon.h"
#import "RTCCommonView.h"
#import "NSBundle+RTCAudioLiveRoom.h"
#import "AudioRoomMixController.h"
#import "SeatInfo.h"
#import "RippleAnimationView.h"
#import "AudioRoomExitController.h"
#import "RTCAudioliveRoom.h"
#import "UIImage+Color.h"
#import "AudioRoomBackgroundMusicController.h"
#import "AudioRoomSoundEffectController.h"
#import "AudioRoomApi.h"
#import "AudioRoomOffLineController.h"
#import "AudioRoomTimeOutController.h"
#import "AudioRoomBackgroundMusic.h"
#import "AudioRoomSoundEffect.h"
#import <AFNetworking/AFNetworking.h>

@interface AudioRoomChatController ()<RTCAudioliveRoomDelegate>

@property (unsafe_unretained, nonatomic) IBOutlet UILabel *roomLabel;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *roomCodeCopyBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *exitBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *offLineBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *muteBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *bgmBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *soundEffectBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *mixButton;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *speakerBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *bgImageView;
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *muteLabel;
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *speakerLabel;
@property (unsafe_unretained, nonatomic) IBOutlet UIView *bottomView;
@property (strong, nonatomic) IBOutletCollection(UIImageView) NSArray *avatorList;
@property (strong, nonatomic) IBOutletCollection(UILabel) NSArray *nickNameList;
@property (strong, nonatomic) AudioRoomBackgroundMusic *music;
@property (strong, nonatomic) AudioRoomSoundEffect *effect1;
@property (strong, nonatomic) AudioRoomSoundEffect *effect2;
@property (strong, nonatomic) AudioRoomMixSetting *mixSetting;
@property (weak, nonatomic) RTCAudioliveRoom *manager;
@property (weak, nonatomic) UIViewController *networkErrorVC;
@property (assign, nonatomic) BOOL isRoomDestroyed;
//@property (strong, nonatomic) NSMutableArray *seats;

@end

@implementation AudioRoomChatController


#pragma mark life cycle

- (instancetype)initWithChanel:(NSString *)channel
                          role:(AliRtcClientRole)role
{
    AudioRoomChatController *vc = [[NSBundle RALR_storyboard] instantiateViewControllerWithIdentifier:@"AudioRoomChatController"];
    vc.manager = [RTCAudioliveRoom sharedInstance];
    vc.manager.delegate = vc;
    vc.channel = channel;
    vc.currentRole = role;
    NSLog(@"我的日志：AudioRoomChatController初始化");
    return vc;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self setupUI];
    [self setNeedsStatusBarAppearanceUpdate];
    [self addNetworkingObserver];
    NSLog(@"我的日志：viewDidLoad");
    
    [self.manager renotifySeatsInfo];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController.navigationBar setHidden:YES];
    if ([self.navigationController respondsToSelector:@selector(interactivePopGestureRecognizer)])
    {
        self.navigationController.interactivePopGestureRecognizer.enabled = NO;
    }
    NSLog(@"我的日志：viewWillAppear");
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.navigationController.navigationBar setHidden:NO];
    if ([self.navigationController respondsToSelector:@selector(interactivePopGestureRecognizer)])
    {
        self.navigationController.interactivePopGestureRecognizer.enabled = YES;
    }
}

- (UIStatusBarStyle) preferredStatusBarStyle
{
    return UIStatusBarStyleLightContent;
}

- (BOOL)shouldAutorotate
{
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
    return  UIInterfaceOrientationMaskPortrait;
}

- (void)dealloc {
    NSLog(@"我的日志 dealloc");
    
}

#pragma mark - private methods

- (void)setupUI
{
    self.bgImageView.image = [NSBundle RALR_pngImageWithName:@"dark-background"];
    [self.roomCodeCopyBtn setImage:[NSBundle RALR_pngImageWithName:@"copy"] forState:UIControlStateNormal];
    [self.exitBtn setImage:[NSBundle RALR_pngImageWithName:@"exit"] forState:UIControlStateNormal];
    
    [self.muteBtn setImage:[NSBundle RALR_pngImageWithName:@"mic-on"] forState:UIControlStateNormal];
    [self.muteBtn setImage:[NSBundle RALR_pngImageWithName:@"mic-off"] forState:UIControlStateSelected];
    
    [self.speakerBtn setImage:[NSBundle RALR_pngImageWithName:@"speaker-off"] forState:UIControlStateNormal];
    [self.speakerBtn setImage:[NSBundle RALR_pngImageWithName:@"volunme-off"] forState:UIControlStateSelected];
    
    [self.bgmBtn setImage:[NSBundle RALR_pngImageWithName:@"background-music"] forState:UIControlStateNormal];
    
    [self.soundEffectBtn setImage:[NSBundle RALR_pngImageWithName:@"sound-effect"] forState:UIControlStateNormal];
    
    [self.mixButton setImage:[NSBundle RALR_pngImageWithName:@"soundconsole"] forState:UIControlStateNormal];
    
    self.roomLabel.text = [NSString stringWithFormat:@"房间号:%@",_channel];
    
    for (UIImageView *imageView in self.avatorList)
    {
        imageView.image = [NSBundle RALR_pngImageWithName:@"empty"];
    }
    
    //    [self setCurrentRole:_currentRole];
}

/// 离开频道
- (void)leaveChannel
{
    [RTCHUD showHUDInView:self.view];
    [self.manager logout];
}

- (void)setupMuteButton:(UIButton *)sender showToast:(BOOL)showToast
{
    if (sender.selected)
    {
        sender.backgroundColor = [UIColor whiteColor];
        self.muteLabel.text = @"取消静音";
        if (showToast)
        {
            [RTCHUD showHud:@"静音模式已开启" inView:self.view];
        }
    } else {
        sender.backgroundColor = [UIColor colorWithRed:1 green:1 blue:1 alpha:0.3];
        self.muteLabel.text = @"静音";
        if (showToast)
        {
            [RTCHUD showHud:@"静音模式已取消" inView:self.view];
        }
    }
}

- (void)showMute:(NSInteger)seatIndex mute:(BOOL)muteMic {
    UIImageView *avator = self.avatorList[seatIndex];
    UIImageView *muteView = [avator viewWithTag:101];
    //添加静音图片
    if(!muteView)  {
        muteView = [[UIImageView alloc] initWithImage:[NSBundle RALR_pngImageWithName:@"mute_red"]];
        muteView.tag = 101;
        [muteView sizeToFit];
        muteView.center = CGPointMake(45, 45);
        [avator addSubview:muteView];
    }
    muteView.hidden = !muteMic;
}

- (void)showSpeaking:(NSInteger)seatIndex speaking:(BOOL)speaking {
    UIImageView *avator = self.avatorList[seatIndex];
    UIView *ripple = [avator.superview viewWithTag:100];
    if(ripple == nil){
        ripple = [[RippleAnimationView alloc] initWithFrame:CGRectMake(0, 0, 52, 52) animationType:AnimationTypeWithBackground];
        [avator.superview addSubview:ripple];
        [avator.superview bringSubviewToFront:avator];
        ripple.tag = 100;
    }
    ripple.hidden  = !speaking;
}

-(void)refreshSeat:(SeatInfo *)seat
{
    NSInteger i = seat.seatIndex;
    UIImageView *avator = self.avatorList[i];
    UILabel *label = self.nickNameList[i];
    avator.image = [NSBundle RALR_pngImageWithName:seat.icon];
    label.text= seat.userName;
    
    //刷新静音图标
    [self showMute:i mute:seat.muteMic];
    
    if (seat.muteMic) {
        seat.speaking = NO;
    }
    //添加波纹效果
    [self showSpeaking:i speaking:seat.speaking];
}

- (void)enterSeat
{
    [AudioRoomApi numberOfUsersInChannel:self.channel
                                complete:^(NSInteger numbers, NSString * _Nonnull error)
     {
        if(error){
            [self dealWithError:error];
            return;
        }
        if (numbers<8 && numbers>=0)
        {
            [self.manager enterSeat];
        } else {
            [self dealWithError:@"上麦人数已满 请稍后"];
        }
    }];
}

- (void)leaveSeat
{
    [self.manager leavelSeat];
}

- (void)dealWithError:(NSString *)error
{
    [RTCHUD hideHUDInView:self.view];
    [RTCHUD showHud:error inView:self.view];
}

- (void)showSdkError:(NSString *)errorMsg
{
    __weak typeof(self) weakSelf = self;
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"错误提示"
                                                                             message:errorMsg
                                                                      preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *confirm = [UIAlertAction actionWithTitle:@"确定"
                                                      style:UIAlertActionStyleDefault
                                                    handler:^(UIAlertAction * _Nonnull action)
                              {
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf.navigationController popViewControllerAnimated:YES];
        });
    }];
    [alertController addAction:confirm];
    [self.navigationController presentViewController:alertController animated:YES completion:nil];
}


/// 注意：不能再下麦的时候 取消静音 因为下麦后停止推流了 调用静音方法无效
- (void)resetMute
{
    //在上麦后 取消静音
    [self.manager muteLocalMic:NO];
    self.muteBtn.selected = NO;
    [self setupMuteButton:self.muteBtn showToast:NO];
}

/// 重置主播设置的状态 比如耳返 伴奏等
/// 注意：不能再下麦的时候 取消静音 因为下麦后停止推流了 调用静音方法无效
- (void)resetRTCStatus
{
    //重置扬声器
    if (self.speakerBtn.selected)
    {
        [self speaker:self.speakerBtn];
    }
    //重置伴奏
    [self.music resetData];
    [self.manager stopAudioAccompany];
    //重置音效
    [self.effect1 resetData];
    [self.effect2 resetData];
    [self.manager stopAudioEffectWithSoundId:self.effect1.effectId];
    [self.manager stopAudioEffectWithSoundId:self.effect2.effectId];
    //重置耳返
    [self.mixSetting resetData];
    [self.manager enableEarBack:self.mixSetting.earBack];
    //重置混响
    [self.manager setAudioEffectReverbMode:self.mixSetting.mixType];
}


- (void)addNetworkingObserver {
    [[AFNetworkReachabilityManager sharedManager] setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
        if (status == AFNetworkReachabilityStatusNotReachable) {
            //显示断网页面
            [self showNetworkErrorPage];
        } else {
            //关闭断网页面
            [self closeNetworkErrorPage];
        }
    }];
    
    [[AFNetworkReachabilityManager sharedManager] startMonitoring];
}

- (void)showNetworkErrorPage
{
    [self dismissViewControllerAnimated:YES completion:nil];
    AudioRoomOffLineController *offLineVC = [[NSBundle RALR_storyboard] instantiateViewControllerWithIdentifier:@"AudioRoomOffLineController"];
    offLineVC.modalPresentationStyle = UIModalPresentationOverFullScreen;
    offLineVC.modalTransitionStyle = UIModalTransitionStyleCrossDissolve;
    [self presentViewController:offLineVC animated:YES completion:nil];
    self.networkErrorVC = offLineVC;
}

- (void)closeNetworkErrorPage
{
    if (self.networkErrorVC) {
        [self dismissViewControllerAnimated:YES completion:nil];
    }
    
    if (self.isRoomDestroyed) {
        [self connectionTimeOut];
    }
}

- (void)connectionTimeOut
{
    [self.manager destroySharedInstance];
    _manager = nil;
    __weak typeof(self) weakSelf = self;
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"提示"
                                                                             message:@"网络重新连接超时,您已退出房间"
                                                                      preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *confirm = [UIAlertAction actionWithTitle:@"确定"
                                                      style:UIAlertActionStyleDefault
                                                    handler:^(UIAlertAction * _Nonnull action)
                              {
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf.navigationController popViewControllerAnimated:YES];
        });
    }];
    [alertController addAction:confirm];
    [self presentViewController:alertController animated:YES completion:nil];
}

#pragma mark - actions

- (IBAction)copyRoomCode:(id)sender
{
    //复制到剪切板
    UIPasteboard *board = [UIPasteboard generalPasteboard];
    board.string = self.channel;
    [RTCHUD showHud:@"房间号已复制" inView:self.view];
}

- (IBAction)exit:(id)sender
{
    __weak typeof(self) weakSelf = self;
    AudioRoomExitController *exitVC = [[NSBundle RALR_storyboard] instantiateViewControllerWithIdentifier:@"AudioRoomExitController"];
    
    [exitVC setComplete:^(int result) {
        if (result) {
            [weakSelf leaveChannel];
        }
    }];
    
    exitVC.modalPresentationStyle = UIModalPresentationOverFullScreen;
    exitVC.modalTransitionStyle = UIModalTransitionStyleCrossDissolve;
    [self presentViewController:exitVC animated:YES completion:nil];
}

- (IBAction)onOffLine:(UIButton *)sender {
    [RTCHUD showHUDInView:self.view];
    
    if (sender.isSelected)
    {
        //下麦
        [self resetMute];
        [self resetRTCStatus];
        [self leaveSeat];
    } else {
        //上麦
        [self enterSeat];
    }
}

- (IBAction)mute:(UIButton *)sender
{
    if (![self.manager muteLocalMic:!sender.selected])
    {
        sender.selected = !sender.selected;
        [self setupMuteButton:sender showToast:YES];
    }
}

- (IBAction)speaker:(UIButton *)sender
{
    if (![self.manager muteAllRemoteAudioPlaying:!sender.selected]) {
        sender.selected = !sender.selected;
        [self setupSpeakButton:sender];
    }
}

- (void)setupSpeakButton:(UIButton *)sender {
    if (sender.selected) {
        sender.backgroundColor = [UIColor whiteColor];
        self.speakerLabel.text = @"开启声音";
        [RTCHUD showHud:@"本地声音已关闭" inView:self.view];
    }else{
        sender.backgroundColor = [UIColor colorWithRed:1 green:1 blue:1 alpha:0.3];
        self.speakerLabel.text = @"关闭声音";
        [RTCHUD showHud:@"本地声音已开启" inView:self.view];
    }
}
- (IBAction)bgm:(id)sender
{
    AudioRoomBackgroundMusicController *mc = [[AudioRoomBackgroundMusicController alloc] initWithMusic:self.music];
    mc.modalPresentationStyle = UIModalPresentationOverFullScreen;
    [self presentViewController:mc animated:YES completion:nil];
}
- (IBAction)soundEffect:(id)sender
{
    
    AudioRoomSoundEffectController *vc = [[AudioRoomSoundEffectController alloc] initWithEffect1:self.effect1 effect2:self.effect2];
    vc.modalPresentationStyle = UIModalPresentationOverFullScreen;
    [self presentViewController:vc animated:YES completion:nil];
}

- (IBAction)mix:(id)sender
{
    AudioRoomMixController *mc = [[AudioRoomMixController alloc] initWithMixSetting:self.mixSetting];
    mc.modalPresentationStyle = UIModalPresentationOverFullScreen;
    [self presentViewController:mc animated:YES completion:nil];
}

#pragma mark - 回调函数
- (void)onPublishResult:(int)result
            isPublished:(BOOL)isPublished
{
    
    
    if (result != 0)
    {
        [RTCHUD hideHUDInView:self.view];
        [RTCHUD showHud:@"流切换失败" inView:self.view];
    }
}
- (void)onOccurError:(int)error
{
    [RTCHUD hideHUDInView:self.view];
    
    //连接超时
    if(error == AliRtcErrIceConnectionReconnectFail)
    {
        [self.manager destroySharedInstance];
        self.manager = nil;
        self.isRoomDestroyed = YES;
        return;
    }
    
    NSString *errMsg = @"";
    if (error == AliRtcErrSdkInvalidState) {
        errMsg = @"sdk 状态错误";
    }else if (error == AliRtcErrIceConnectionHeartbeatTimeout) {
        errMsg = @"信令心跳超时";
    }else if (error == AliRtcErrSessionRemoved) {
        errMsg = @"Session 已经被移除，Session 不存在";
    }
    //发生以上错误的时候 需要销毁manager
    if (errMsg.length) {
        [self.manager destroySharedInstance];
        self.manager = nil;
        [self showSdkError:errMsg];
        return;
    }
    errMsg = [NSString stringWithFormat:@"错误码:%d",error];
    [RTCHUD showHud:errMsg inView:self.view];
    
}


- (void)onOccurWarning:(int)warn {
}

- (void)onUpdateRoleNotifyWithOldRole:(AliRtcClientRole)oldRole
                              newRole:(AliRtcClientRole)newRole
{
    self.currentRole = newRole;
}

- (void)onSeatVolumeChanged:(NSInteger)seatIndex isSpeaking:(BOOL)isSpeaking
{
    
    [self showSpeaking:seatIndex speaking:isSpeaking];
}

- (void)onSeatMutedChanged:(NSInteger)seatIndex mute:(BOOL)mute
{
    //显示静音
    [self showMute:seatIndex mute:mute];
    //关闭动画
    if (mute) {
        [self showSpeaking:seatIndex speaking:NO];
    }
    
}

- (void)onEnterSeat:(SeatInfo *)seat
{
    NSLog(@"我的日志：onEnterSeat");
    [self refreshSeat:seat];
}

- (void)onLeaveSeat:(SeatInfo *)seat
{
    NSLog(@"我的日志：onLeaveSeat");
    seat.userId = @"";
    [self refreshSeat:seat];
    
}

- (void)onAudioPlayingStateChanged:(AliRtcAudioPlayingStateCode)playState errorCode:(AliRtcAudioPlayingErrorCode)errorCode
{
    if (playState == AliRtcAudioPlayingEnded)
    {
        self.music.publishing = NO;
        self.music.testing = NO;
    }
}

- (void)onLeaveChannelResult:(int)result
{
    [RTCHUD hideHUDInView:self.view];
    
    if (result == 0)
    {
        if(!self.isRoomDestroyed)
        {
            [self.navigationController popViewControllerAnimated:YES];
        }
    }
}

- (void)onRoomDestory
{
    
    [self.manager destroySharedInstance];
    self.manager = nil;
    self.isRoomDestroyed = YES;
    
    [self dismissViewControllerAnimated:NO completion:nil];
    //房间销毁
    AudioRoomTimeOutController *exitVC = [[NSBundle RALR_storyboard] instantiateViewControllerWithIdentifier:@"AudioRoomTimeOutController"];
    exitVC.modalPresentationStyle = UIModalPresentationOverFullScreen;
    exitVC.modalTransitionStyle = UIModalTransitionStyleCrossDissolve;
    [self presentViewController:exitVC animated:YES completion:nil];
    
    
}
#pragma mark - getter & sertter

//- (RTCAudioliveRoom *)manager
//{
//    if (!_manager) {
//        _manager = [RTCAudioliveRoom sharedInstance];
//    }
//    return _manager;
//}

- (void)setCurrentRole:(AliRtcClientRole)currentRole
{
    _currentRole = currentRole;
    [RTCHUD hideHUDInView:self.view];
    if (self.currentRole == AliRtcClientRoleInteractive)
    {
        //按钮变成选中的
        self.bottomView.hidden = NO;
        self.offLineBtn.selected = YES;
        [self.offLineBtn setTitle:@"下麦" forState:UIControlStateNormal];
    } else {
        //按钮变成未选中的
        self.bottomView.hidden = YES;
        self.offLineBtn.selected = NO;
        [self.offLineBtn setTitle:@"上麦" forState:UIControlStateNormal];
    }
}

- (AudioRoomBackgroundMusic *)music
{
    if (!_music) {
        _music = [[AudioRoomBackgroundMusic alloc] init];
    }
    return _music;
}

- (AudioRoomSoundEffect *)effect1
{
    if (!_effect1)
    {
        _effect1 = [[AudioRoomSoundEffect alloc] initWithEffectId:1000
                                                         fileName:@"smile.mp3"];
    }
    return _effect1;
}

- (AudioRoomSoundEffect *)effect2
{
    if (!_effect2) {
        _effect2 = [[AudioRoomSoundEffect alloc] initWithEffectId:2000
                                                         fileName:@"Clapping.mp3"];
    }
    return _effect2;
}

- (AudioRoomMixSetting *)mixSetting
{
    if (!_mixSetting)
    {
        _mixSetting = [[AudioRoomMixSetting alloc] init];
    }
    
    return _mixSetting;
}

@end
