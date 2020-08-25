//
//  AlivcVoiceCallChannelViewController.m
//  VoiceCall
//
//  Created by aliyun on 2020/4/8.
//

#import "VoiceCallChannelViewController.h"
#import <AliRTCSdk/AliRTCSdk.h>
#import "UIViewController+Alert.h"
#import "VoiceCallChattingViewController.h"
#import "VideoCallUserAuthrization.h"
#import "IndicatorButton.h"
#import "NSBundle+VoiceCall.h"
#import "UIImage+Color.h"

typedef enum : NSUInteger {
    AlivcVoiceCallChannelInputStateBeginning, //初始状态 未编辑
    AlivcVoiceCallChannelInputStateActive,   //输入框激活状态 输入框没有文字
    AlivcVoiceCallChannelInputStateEditting, //输入框有文字
    AlivcVoiceCallChannelInputStateError,   // 校验输入不合法
} AlivcVoiceCallChannelInputState;



@interface VoiceCallChannelViewController ()<UITextFieldDelegate,AliRtcEngineDelegate>

/**
 @brief 房间号textfiled
 */
@property(nonatomic, weak) UITextField *tfChannel;
/**
 @brief 房间号标题
 */
@property(nonatomic, weak) UILabel *roomTitle;
/**
 @brief 占位文字
 */
@property(nonatomic, weak) UILabel *placeHolder;
/**
 @brief 输入框下横线
 */
@property(nonatomic, weak) UIView *underline;
/**
 @brief 加入房间button
 */
@property(nonatomic, strong) IndicatorButton  *btnJoin;
/**
 @brief 房间输入框的状态
 */
@property (nonatomic,assign) AlivcVoiceCallChannelInputState channelState;
/**
 @brief SDK实例
 */
@property (nonatomic, strong) AliRtcEngine *engine; 

@end

@implementation VoiceCallChannelViewController

#pragma mark - life cycle

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    //    self.preferredStatusBarStyle = UIStatusBarStyleDefault;
    //导航栏名称等基本设置
    [self baseSetting];
    //添加页面控件
    [self addSubviews];
    //添加手势
    [self addGesture];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationController.navigationBar.tintColor = [UIColor blackColor];
    _engine = nil;
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (UIStatusBarStyle) preferredStatusBarStyle {
    if (@available(iOS 13.0, *)) {
        return UIStatusBarStyleDarkContent;
    } else {
        return UIStatusBarStyleDefault;
    }
}

#pragma mark - UITextField delegate

-(BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    
    if(textField.text.length) {
        self.channelState = AlivcVoiceCallChannelInputStateEditting;
    }else {
        self.channelState  = AlivcVoiceCallChannelInputStateBeginning;
    }
    
    [self onBtnJoin:self.btnJoin];
    return YES;
}
- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField {
    if (textField.text.length) {
        self.channelState = AlivcVoiceCallChannelInputStateEditting;
    } else {
        self.channelState = AlivcVoiceCallChannelInputStateActive;
    }
    
    return YES;
}

- (BOOL)textFieldShouldEndEditing:(UITextField *)textField {
    
    if(textField.text.length) {
        self.channelState = AlivcVoiceCallChannelInputStateEditting;
    }else {
        self.channelState  = AlivcVoiceCallChannelInputStateBeginning;
    }
    return YES;
}
- (void)changedTextField:(UITextField *)textField {
    if (textField.text.length) {
        self.channelState = AlivcVoiceCallChannelInputStateEditting;
    }else{
        self.channelState = AlivcVoiceCallChannelInputStateActive;
    }
}

#pragma mark - action

- (void)onBtnJoin:(IndicatorButton *)sender {
    [sender startLoading];
    [self.view endEditing:YES];
    NSString *channelName = [_tfChannel.text stringByReplacingOccurrencesOfString:@" " withString:@""];
    
    if (![self legalChannel]) {
        [self showMessage:@"房间号格式错误" inView:self.view];
        self.underline.backgroundColor = [UIColor redColor];
        [sender stopLoading];
        self.channelState = AlivcVoiceCallChannelInputStateError;
        return;
    }
    
    
     [self joinChannel:channelName sender:sender];
 
}

//回收键盘
- (void)endTextFeldEdite:(UITapGestureRecognizer *)tap {
    [self.view endEditing:YES];
}

#pragma mark - private

/**
 @brief 添加手势
 */
- (void)addGesture{
    //点击屏幕->回收键盘
    UITapGestureRecognizer *endEditeTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(endTextFeldEdite:)];
    [self.view addGestureRecognizer:endEditeTap];
}


- (BOOL)legalChannel {
    NSString *pattern = @"^[0-9a-zA-Z_-]{1,64}$";
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"SELF MATCHES %@", pattern];
    BOOL isMatch = [pred evaluateWithObject:_tfChannel.text];
    return isMatch;
}


/**
 @brief 加入频道
 */
- (void)joinChannel:(NSString *)channelName sender:(IndicatorButton *)sender {
    //AliRtcAuthInfo:各项参数均需要客户App Server(客户的server端) 通过OpenAPI来获取，然后App Server下发至客户端，客户端将各 项参数赋值后，即可joinChannel
    [VideoCallUserAuthrization getPassportFromAppServer:channelName  block:^(AliRtcAuthInfo *AuthInfo, NSError *error) {
//        NSString *name = [[NSUserDefaults standardUserDefaults] objectForKey:ALIVC_USERNAME];
        if(error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self showMessage:@"网络请求失败，请检查您的网络设置" inView:self.view];
                [sender stopLoading];
            });
            return;
        }
        [VideoCallUserAuthrization userListInChannel:channelName block:^(NSArray *users, NSError *error) {
            if(error) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self showMessage:@"网络请求失败，请检查您的网络设置" inView:self.view];
                    [sender stopLoading];
                });
                return;
            }
            if (users.count <= 1) {
                NSString *userName = [NSString stringWithFormat:@"iOSUser%d",arc4random()%1234];
                [self.engine joinChannel:AuthInfo name:userName onResult:^(NSInteger errCode) {
                    //加入频道Ô回调处理
                    NSLog(@"joinChannel result: %d", (int)errCode);
                    if (errCode == 0) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            VoiceCallChattingViewController *chatVC = [[VoiceCallChattingViewController alloc] initWithChannelName:channelName appId:AuthInfo.appid userId:AuthInfo.user_id];
                            UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:chatVC];
                            nav.modalPresentationStyle = UIModalPresentationFullScreen;
                            [self presentViewController:nav  animated:YES completion:nil];
                            
                            self.channelState = AlivcVoiceCallChannelInputStateBeginning;
                            [sender stopLoading];
                        });
                    } else {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [self showMessage:@"房间加入失败" inView:self.view];
                            [sender stopLoading];
                        });
                    }
                }];
            } else{
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self showMessage:@"房间已满员" inView:self.view];
                    self.channelState = AlivcVoiceCallChannelInputStateBeginning;
                    [sender stopLoading];
                });
            }
        }];
    }];
}

#pragma mark - UI setting

- (void)baseSetting{
    self.view.backgroundColor = [UIColor whiteColor];
    UIImage *image = [UIImage imageWithColor:[[UIColor whiteColor]colorWithAlphaComponent:0]];
    [self.navigationController.navigationBar setBackgroundImage:image forBarMetrics:UIBarMetricsDefault];
    [self.navigationController.navigationBar setShadowImage:image];
    

    UIBarButtonItem *leftItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"angle_left"] style:UIBarButtonItemStylePlain target:self action:@selector(back)];
    self.navigationItem.leftBarButtonItem = leftItem;
}

- (void)addSubviews {
    CGRect rect = [UIScreen mainScreen].bounds;
    CGFloat screenWidth = rect.size.width;
    CGFloat screenHeight = rect.size.height;
//    //logo
    CGFloat logoW = screenWidth;
    CGFloat logoH = (logoW * 88)/125;
    CGFloat logoX = 0;
    CGFloat logoY = 0;
    UIImageView *logoView = [[UIImageView alloc] initWithFrame:CGRectMake(logoX, logoY, logoW, logoH)];
//    logoView.backgroundColor = [UIColor redColor];
    [self.view addSubview:logoView];
    logoView.image = [UIImage imageNamed:@"background"];
    //总的高度是268
    CGFloat y = (screenHeight  - 250) * 0.5;
    
    //slogan
    UILabel *slogan = [[UILabel alloc] init];
    slogan.text = @"1对1语音通话";
    slogan.font = [UIFont fontWithName:@"PingFangSC-Medium" size:24];
    slogan.textColor = [UIColor colorWithRed:38/255.0 green:38/255.0 blue:38/255.0 alpha:1/1.0];
    [slogan sizeToFit];
    slogan.center =  CGPointMake(screenWidth * 0.5, y);
    [self.view addSubview:slogan];
     
    
    //roomTitle
    CGFloat roomTitleX = 18;
    CGFloat roomTitleY = CGRectGetMaxY(slogan.frame) +30;
    UILabel *roomTitle = [[UILabel alloc] init];
    roomTitle.frame = CGRectMake(roomTitleX, roomTitleY, 80, 10);
    roomTitle.text = @"房间号";
    roomTitle.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    roomTitle.textColor = [UIColor colorWithRed:140/255.0 green:140/255.0 blue:140/255.0 alpha:1/1.0];
    roomTitle.hidden = YES;
    [self.view addSubview:roomTitle];
    self.roomTitle = roomTitle;
    
    //placeHolder
    CGFloat placeHolderY = roomTitleY + 18;
    UILabel *placeHolder = [[UILabel alloc] init];
    placeHolder.frame = CGRectMake(roomTitleX,placeHolderY, 80, 11);
    placeHolder.text = @"房间号";
    placeHolder.font = [UIFont fontWithName:@"PingFangSC-Regular" size:16];
    placeHolder.textColor = [UIColor colorWithRed:38/255.0 green:38/255.0 blue:38/255.0 alpha:1/1.0];
    [placeHolder sizeToFit];
    [self.view addSubview:placeHolder];
    self.placeHolder = placeHolder;
    
    //channelTF
    CGFloat channelTFX = roomTitleX;
    CGFloat channelTFW = screenWidth - 2 * channelTFX;
    UITextField *channelTF = [[UITextField alloc] initWithFrame:CGRectMake(roomTitleX, placeHolderY, channelTFW, 34)];
    channelTF.font = [UIFont fontWithName:@"PingFangSC-Regular" size:16];
    channelTF.delegate = self;
    channelTF.returnKeyType = UIReturnKeyGo;
    channelTF.clearButtonMode = UITextFieldViewModeWhileEditing;
    [channelTF addTarget:self action:@selector(changedTextField:) forControlEvents:UIControlEventEditingChanged];
    
    [self.view addSubview:channelTF];
    self.tfChannel = channelTF;
    
    //underline
    CGFloat underlineX = roomTitleX;
    CGFloat underlineY = CGRectGetMaxY(channelTF.frame) + 11;
    CGFloat underlineW = screenWidth - 2 * underlineX;
    CGFloat scale = [[UIScreen mainScreen] scale];
    CGFloat height = 1 / scale;
    UIView *underline = [[UIView alloc] initWithFrame:CGRectMake(underlineX, underlineY, underlineW, height)];
    underline.backgroundColor =  [UIColor colorWithRed:151/255.0 green:151/255.0 blue:151/255.0 alpha:1/1.0];
    [self.view addSubview:underline];
    self.underline = underline;
    
    
    //notice
    CGFloat noticeX = underlineX;
    CGFloat noticeY = CGRectGetMaxY(underline.frame) + 11;
    UILabel *notice = [[UILabel alloc] init];
    notice.frame = CGRectMake(noticeX, noticeY, 300, 40);
    notice.text = @"1~64位，支持大小写字母、数字、下划线_ \n注：相同房间号才可以进行实时连麦";
    notice.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
    notice.textColor = [UIColor colorWithRed:89/255.0 green:89/255.0 blue:89/255.0 alpha:1/1.0];
    notice.numberOfLines = 0;
    [notice sizeToFit];
    [self.view addSubview:notice];
    
    
    //chattingButton
    CGFloat indicatorButtonX = noticeX;
    CGFloat indicatorButtonY = CGRectGetMaxY(notice.frame) + 75;
    CGFloat indicatorButtonW = screenWidth - 2 * indicatorButtonX;
    CGFloat indicatorButtonH = 48;
    IndicatorButton *btnJoin = [IndicatorButton buttonWithType:UIButtonTypeCustom];
    [btnJoin setTitle:@"开始通话" forState:UIControlStateNormal];
    [btnJoin setTitle:@"加载中" forState:UIControlStateSelected];
    btnJoin.frame = CGRectMake(indicatorButtonX, indicatorButtonY, indicatorButtonW, indicatorButtonH);
    [btnJoin setBackgroundColor:[UIColor colorWithRed:1/255.0 green:62/255.0 blue:190/255.0 alpha:1/1.0] forState:UIControlStateNormal];
    
    [btnJoin setBackgroundColor:[UIColor colorWithRed:182/255.0 green:197/255.0 blue:233/255.0 alpha:1/1.0] forState:UIControlStateDisabled];
    [btnJoin addTarget:self action:@selector(onBtnJoin:) forControlEvents:UIControlEventTouchUpInside];
    btnJoin.layer.cornerRadius = 24;
    btnJoin.layer.masksToBounds = YES;
    btnJoin.enabled = NO;
    [self.view addSubview:btnJoin];
    self.btnJoin = btnJoin;
}
- (void)back {
    [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - setter getter
- (AliRtcEngine *)engine {
    if (!_engine) {
        _engine = [AliRtcEngine sharedInstance:nil extras:@""];
        [_engine setAudioOnlyMode:YES];
    }
    return _engine;
}


- (void)setChannelState:(AlivcVoiceCallChannelInputState)channelState {
    _channelState = channelState;
    switch (channelState) {
        case AlivcVoiceCallChannelInputStateBeginning:
            self.tfChannel.text = @"";
            self.placeHolder.hidden = NO;
            self.roomTitle.hidden = YES;
            self.btnJoin.enabled = NO;
            self.underline.backgroundColor = [UIColor colorWithRed:151/255.0 green:151/255.0 blue:151/255.0 alpha:1/1.0];
            break;
        case AlivcVoiceCallChannelInputStateActive:
            self.placeHolder.hidden = YES;
            self.roomTitle.hidden = NO;
            self.btnJoin.enabled = NO;
            self.underline.backgroundColor = [UIColor colorWithRed:151/255.0 green:151/255.0 blue:151/255.0 alpha:1/1.0];
            break;
        case AlivcVoiceCallChannelInputStateEditting:
            self.placeHolder.hidden = YES;
            self.roomTitle.hidden = NO;
            self.btnJoin.enabled = YES;
            self.underline.backgroundColor = [UIColor colorWithRed:151/255.0 green:151/255.0 blue:151/255.0 alpha:1/1.0];
            break;
        case AlivcVoiceCallChannelInputStateError:
            self.placeHolder.hidden = YES;
            self.roomTitle.hidden = NO;
            self.btnJoin.enabled = YES;
            self.underline.backgroundColor = [UIColor redColor];
            break;
        default:
            break;
    }
}

@end
