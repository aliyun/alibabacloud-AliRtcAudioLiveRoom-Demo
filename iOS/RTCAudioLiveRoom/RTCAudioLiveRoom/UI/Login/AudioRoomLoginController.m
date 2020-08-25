//
//  AudioLiveRoomLoginController.m
//  AFNetworking
//
//  Created by Aliyun on 2020/6/30.
//

#import "AudioRoomLoginController.h"
#import "NSBundle+RTCAudioLiveRoom.h"
#import "AudioRoomChatController.h"
#import "UIImage+Color.h"
#import "RTCCommonView.h"
#import "RTCCommon.h"
#import "AudioRoomApi.h"
#import "RTCAudioliveRoom.h"
#import "AudioRoomOffLineController.h"

@interface AudioRoomLoginController ()<UITextFieldDelegate>

@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *logoBackgroundView;
@property (unsafe_unretained, nonatomic) IBOutlet UIView *codeViewContainer;
@property (unsafe_unretained, nonatomic) IBOutlet UITextField *nickNameLabel;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *connectRoleButton;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *audienceRoleButton;
@property (unsafe_unretained, nonatomic) IBOutlet LoadingButton *loginButton;
@property (assign, nonatomic) AliRtcClientRole roleType;
@property (strong, nonatomic) CodeInputView *codeView;
@property (weak, nonatomic) RTCAudioliveRoom *manager;
@property (weak, nonatomic) UIViewController *networkErrorVC;
@end

@implementation AudioRoomLoginController

#pragma mark - system methods

- (instancetype)init
{
    UIStoryboard *storyboard = [NSBundle RALR_storyboard];
    return [storyboard instantiateViewControllerWithIdentifier:@"AudioRoomLoginController"];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self setupUI];
    [self requsetAuthInfo];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.navigationController.navigationBar.tintColor = [UIColor blackColor];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    [self.view endEditing:YES];
}

- (BOOL)shouldAutorotate
{
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
    return  UIInterfaceOrientationMaskPortrait;
}

- (UIStatusBarStyle) preferredStatusBarStyle
{
    if (@available(iOS 13.0, *)) {
        return UIStatusBarStyleDarkContent;
    } else {
        return UIStatusBarStyleDefault;
    }
}

#pragma mark - private methods

- (void)setupUI
{
    //navigationbar
    UIImage *image = [UIImage imageWithColor:[[UIColor whiteColor]colorWithAlphaComponent:0]];
    [self.navigationController.navigationBar setBackgroundImage:image forBarMetrics:UIBarMetricsDefault];
    [self.navigationController.navigationBar setShadowImage:image];
    
    UIBarButtonItem *leftItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"angle_left"] style:UIBarButtonItemStylePlain target:self action:@selector(back)];
    self.navigationItem.leftBarButtonItem = leftItem;
    
    //codeView
    __weak typeof(self) weakSelf = self;
    CGFloat width  = [UIScreen mainScreen].bounds.size.width - 36 * 2;
    CGFloat height = self.codeViewContainer.bounds.size.height;
    CodeInputView *codeView = [[CodeInputView alloc] initWithFrame:CGRectMake(0, 0, width, height) inputType:5 selectCodeBlock:^(NSString *code){
        if(code.length != 5) {
//            weakSelf.authInfo = nil;
        }
        [weakSelf checkLoginButtonStatus];
    }];
    [self.codeViewContainer addSubview:codeView];
    self.codeView = codeView;
    [self.codeView endEdit];
    
    self.nickNameLabel.delegate = self;
    [self.nickNameLabel addTarget:self action:@selector(textFieldDidChange:) forControlEvents:UIControlEventEditingChanged];
    
    //role bttton
    self.roleType = AliRtcClientRoleInteractive;
    [self.connectRoleButton setImage:[NSBundle RALR_pngImageWithName:@"connector-notselected"] forState:UIControlStateNormal];
    [self.connectRoleButton setImage:[NSBundle RALR_pngImageWithName:@"connector-notselected"] forState:UIControlStateHighlighted];
    [self.connectRoleButton setImage:[NSBundle RALR_pngImageWithName:@"connector-selected"] forState:UIControlStateSelected];
    
    [self.audienceRoleButton setImage:[NSBundle RALR_pngImageWithName:@"audience-notselected"] forState:UIControlStateNormal];
    [self.audienceRoleButton setImage:[NSBundle RALR_pngImageWithName:@"audience-notselected"] forState:UIControlStateHighlighted];
    [self.audienceRoleButton setImage:[NSBundle RALR_pngImageWithName:@"audience-selected"] forState:UIControlStateSelected];
    
    //loginButton
    [_loginButton setBackgroundColor:[UIColor colorWithRed:1/255.0 green:62/255.0 blue:190/255.0 alpha:1/1.0] forState:UIControlStateNormal];
    
    [_loginButton setBackgroundColor:[UIColor colorWithRed:182/255.0 green:197/255.0 blue:233/255.0 alpha:1/1.0] forState:UIControlStateDisabled];
    [_loginButton addTarget:self action:@selector(login:) forControlEvents:UIControlEventTouchUpInside];
    _loginButton.layer.cornerRadius = 24;
    _loginButton.layer.masksToBounds = YES;
    _loginButton.enabled = NO;
    
}



- (void)checkLoginButtonStatus
{
    if (self.nickNameLabel.text.length>0 && self.codeView.text.length == 5)
    {
        self.loginButton.enabled = YES;
    } else {
        self.loginButton.enabled = NO;
    }
    
}

#pragma mark - network requeset

- (void)requsetAuthInfo
{
    [AudioRoomApi randomAuthInfo:^(AliRtcAuthInfo * _Nonnull info,NSString *nickName, NSString * _Nonnull errorMsg)
     {
        if (!errorMsg)
        {
            [self dealWithAuthInfo:info name:nickName];
        } else {
            [self dealWithError:errorMsg];
        }
    }];
}

- (void)dealWithAuthInfo:(AliRtcAuthInfo *)info
                    name:(NSString *)nickName
{
    self.nickNameLabel.text = nickName;
    self.codeView.text = info.channel;
}



#pragma mark - actions

- (void)back
{
    [[RTCAudioliveRoom sharedInstance] destroySharedInstance];
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)roleClicked:(UIButton *)sender
{
    if (sender.tag == 1) {
        //互动角色
        self.connectRoleButton.selected = YES;
        self.audienceRoleButton.selected = NO;
        self.roleType = AliRtcClientRoleInteractive ;
    } else {
        //观众
        self.connectRoleButton.selected = NO;
        self.audienceRoleButton.selected = YES;
        self.roleType = AliRtcClientRolelive;
    }
}

- (void)login:(LoadingButton *)button
{
    
     [button startLoading];
    
    //判断人数
    if (self.roleType == AliRtcClientRoleInteractive)
    {
        __weak typeof(self) weakSelf = self;
        [AudioRoomApi numberOfUsersInChannel: self.codeView.text complete:^(NSInteger numbers, NSString * _Nonnull error)
         {
            if(error)
            {
                [weakSelf dealWithError:error];
                return;
            }
            if (numbers < 8 && numbers >=0)
            {
                [weakSelf joinChannel];
            } else {
                [weakSelf dealWithError:@"上麦人数已满请稍后"];
            }
        }];
        
        return;
    }
    
    [self joinChannel];
}

- (void)joinChannel
{
    __weak typeof(self) weakSelf = self;
    NSString *nickName = self.nickNameLabel.text;
    [self.manager login:self.codeView.text
                   name:nickName
                   role:self.roleType
               complete:^(AliRtcAuthInfo * _Nonnull authInfo, NSInteger errorCode)
     {
        if (authInfo)
        {
            [AudioRoomApi joinChannelSuccess:self.codeView.text
                                      userId:authInfo.user_id
                                    nickName:nickName
                                    complete:^(NSString * _Nonnull errormsg){}];
            [weakSelf joinSuccess];
            
            return;
        }
        [weakSelf dealWithError:@"加入房间失败"];
    }];
}

- (void)joinSuccess
{
    [self.loginButton stopLoading];
    AudioRoomChatController *vc = [[AudioRoomChatController alloc] initWithChanel:self.codeView.text role:self.roleType];
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)dealWithError:(NSString *)error
{
    [self.loginButton stopLoading];
    //显示错误框
    if ([error containsString:@"-1009"]) {
        [RTCHUD showHud:@"网络连接失败，无法进入房间" inView:self.view];
        return;
    }
    [RTCHUD showHud:error inView:self.view];
}


#pragma mark - textFieldDelegat

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}

- (void)textFieldDidChange:(id)sender
{
    [self checkLoginButtonStatus];
}

#pragma mark - setter & getter
- (RTCAudioliveRoom *)manager
{
    if (!_manager)
    {
        _manager =  [RTCAudioliveRoom sharedInstance];
    }
    
    return _manager;
}


@end
