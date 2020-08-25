//
//  AlivcVoiceCallSettingViewController.m
//  VoiceCall
//
//  Created by aliyun on 2020/4/10.
//

#import "VoiceCallSettingViewController.h"
#import "NSBundle+VoiceCall.h"

@interface VoiceCallSettingViewController ()
/**
@brief 选中的音乐按钮
*/
@property (weak,nonatomic) UIButton *selectedMusicBtn;
/**
@brief 选中的主题按钮
*/
@property (weak,nonatomic) UIButton *selectedThemeBtn;
/**
@brief 背景集合
*/
@property (strong,nonatomic) NSArray *themes;
/**
@brief 背景名称集合
*/
@property (strong,nonatomic) NSArray *themeNames;
/**
@brief 音乐集合
*/
@property (strong,nonatomic) NSArray *musics;

@end

@implementation VoiceCallSettingViewController

#pragma mark - life cycle
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self initalData];
    [self baseSetting];
    
}

#pragma mark - init data
- (void)initalData {
    _themes = @[@"主题1",@"主题2",@"主题3"];
    _themeNames = @[@"霜白",@"酷黑",@"繁星"];
    _musics = @[@"无音乐",@"Action Epic",@"Ice Cream with you",@"Yippee"];
}
#pragma mark - save Data
/**
@brief 保存数据到沙盒
*/
- (void)saveData{
    NSString *themeName = _themes[_selectedThemeBtn.tag] ;
    [[NSUserDefaults standardUserDefaults] setObject:themeName forKey:ALIVC_SelectedThemeName];
    NSString *musicName = _musics[_selectedMusicBtn.tag];
    NSString *musicId = [NSString stringWithFormat:@"%ld",(long)_selectedMusicBtn.tag];
    [[NSUserDefaults standardUserDefaults] setObject:musicName forKey:ALIVC_SelectedMusicName];
    [[NSUserDefaults standardUserDefaults] setObject:musicId forKey:ALIVC_SelectedMusicId];
    [[NSUserDefaults standardUserDefaults] synchronize];
    
    NSLog(@"选中了主题:%@ 音乐：%@",themeName,musicName);
    
    [[NSNotificationCenter defaultCenter] postNotificationName:ALIVC_RefreshBackgroudAndMusic object:nil];
}

#pragma mark - actions
/**
@brief 音乐Item点击
*/
- (void)musicItemClick:(UIGestureRecognizer *)gesture {
    UIView *touchedView = gesture.view;
    for (UIView *view in touchedView.subviews) {
        if ([view isKindOfClass:[UIButton class]]) {
            self.selectedMusicBtn = (UIButton *)view;
        }
    }
}
/**
@brief 音乐item内的按钮点击
*/
- (void)musicButtonClick:(UIButton *)button {
    button.selected = !button.selected;
    if (button.selected) {
        NSLog(@"恢复音乐:%ld",(long)button.tag);
        [self resumeLocalMusic:button.tag];
    }else{
        NSLog(@"暂停音乐:%ld",(long)button.tag);
        [self pauseLocalMusic:button.tag];
    }
    
}
/**
@brief 主题按钮点击
*/
- (void)themeClicked:(UIButton *)sender {
    self.selectedThemeBtn = sender;
}

/**
@brief 关闭按钮点击
*/
- (void)dismiss {
    [self stopLocalMusic:self.selectedMusicBtn.tag];
    [self saveData];
    if (self.delegate && [self.delegate respondsToSelector:@selector(dismissModalViewController:)]) {
        [self.delegate dismissModalViewController:self];
    }
}

#pragma mark - sdk music actions
- (void)playLoaclMusic:(NSString *)musicName soundId:(NSInteger)soundId {
    NSString *path = [NSBundle musicPathForResource:musicName];
    if(![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        return;
    }
    NSString *url = [path stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    [self.engine preloadAudioEffectWithSoundId:soundId filePath:url];
    [self.engine playAudioEffectWithSoundId:soundId filePath:url cycles:-1 publish:NO];
}


- (void)pauseLocalMusic:(NSInteger)soundId {
    [self.engine pauseAudioEffectWithSoundId:soundId];
}

- (void)resumeLocalMusic:(NSInteger)soundId {
     [self.engine resumeAudioEffectWithSoundId:soundId];
}

- (void)stopLocalMusic:(NSInteger)soundId {
    [self.engine stopAudioEffectWithSoundId:soundId];
}


#pragma mark - setter
- (void)setSelectedMusicBtn:(UIButton *)selectedMusicBtn {
    _selectedMusicBtn.enabled = NO;
    _selectedMusicBtn.selected = NO;
    [self stopLocalMusic:_selectedMusicBtn.tag];
    
    _selectedMusicBtn = selectedMusicBtn;
    _selectedMusicBtn.enabled = YES;
    _selectedMusicBtn.selected = YES;
    [self playLoaclMusic:_musics[_selectedMusicBtn.tag] soundId:_selectedMusicBtn.tag];
    
    NSInteger index = selectedMusicBtn.tag;
    NSLog(@"选中了音乐:%ld 播放了音乐：%@",(long)index,_musics[index]);
    NSLog(@"%@",[NSBundle musicPathForResource:_musics[index]]);
}

- (void)setSelectedThemeBtn:(UIButton *)selectedThemeBtn {
    
    UIButton *selectedView = [_selectedThemeBtn viewWithTag:100];
    selectedView.selected = NO;
    
    _selectedThemeBtn = selectedThemeBtn;
    
    selectedView = [_selectedThemeBtn viewWithTag:100];
    selectedView.selected = YES;
    
    NSLog(@"选中了主题:%@",_themes[selectedThemeBtn.tag]);
}

#pragma mark - UI operation

- (void)addThemeWithPositionX:(CGFloat)itemX tag:(NSInteger)tag themeName:(NSString *)themeName themeTips:(UILabel *)themeTips themeTitle:(NSString *)themeTitle  selected:(BOOL)selected{
    CGFloat itemY = CGRectGetMaxY(themeTips.frame) + 18;
    CGFloat itemWidth = 100;
    CGFloat itemHeight = 130;
    UIButton *themeItem = [UIButton buttonWithType:UIButtonTypeCustom];
    themeItem.frame = CGRectMake(itemX, itemY, itemWidth, itemHeight);
    [themeItem setImage:[NSBundle pngImageWithName:themeName] forState:UIControlStateNormal];
    [themeItem setImage:[NSBundle pngImageWithName:themeName] forState:UIControlStateHighlighted];
    themeItem.tag = tag;
    [themeItem addTarget:self action:@selector(themeClicked:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:themeItem];
    
    CGFloat space = 6;
    CGFloat selectedVieW = 21;
    CGFloat selectedVieH = 21;
    CGFloat selectedViewX = itemWidth - space - selectedVieW;
    CGFloat selectedViewY = itemHeight - space - selectedVieH;
    
    UIButton *selectedView = [[UIButton alloc] initWithFrame:CGRectMake(selectedViewX, selectedViewY, selectedVieW, selectedVieH)];
    selectedView.tag = 100;
    [themeItem addSubview:selectedView];
    [selectedView setImage: [NSBundle pngImageWithName:@"check"] forState:UIControlStateNormal];
    [selectedView setImage: [NSBundle pngImageWithName:@"check_filled"] forState:UIControlStateSelected];
    selectedView.userInteractionEnabled = NO;
    selectedView.selected = selected;
    
    if (selected) {
        self.selectedThemeBtn = themeItem;
    }
    
    
    CGFloat itemTitleX = itemX;
    CGFloat itemTitleY = CGRectGetMaxY(themeItem.frame) + 9;
    CGFloat itemTitleW = itemWidth;
    CGFloat itemTitleH = 22;
    
    UILabel *itemTitle = [[UILabel alloc] init];
    itemTitle.frame = CGRectMake(itemTitleX, itemTitleY, itemTitleW, itemTitleH);
    itemTitle.text = themeTitle;
    itemTitle.font = [UIFont fontWithName:@"PingFangSC-Regular" size:13];
    itemTitle.textColor = [UIColor colorWithRed:38/255.0 green:38/255.0 blue:38/255.0 alpha:1/1.0];
    itemTitle.textAlignment = NSTextAlignmentCenter;
    [self.view addSubview:itemTitle];
}

- (void)addMusicItem:(CGFloat)musicItemH musicItemY:(CGFloat)musicItemY musicName:(NSString *)musicName selected:(BOOL)selected tag:(NSInteger)tag{
    CGFloat musicItemX = 16;
    CGFloat musicItemW = [UIScreen mainScreen].bounds.size.width - musicItemX;
    
    UIView *musicItem = [[UIView alloc] init];
    musicItem.frame = CGRectMake(musicItemX, musicItemY, musicItemW, musicItemH);
    [self.view addSubview:musicItem];
    UIGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(musicItemClick:)];
    [musicItem addGestureRecognizer:tap];
    
    CGFloat scale = [[UIScreen mainScreen] scale];
    CGFloat underlineH = 1 / scale;
    CGFloat underlineW = musicItemW;
    CGFloat underlineX = 0;
    CGFloat underlineY = musicItemH - underlineH;
    UIView *lineView = [[UIView alloc] initWithFrame:CGRectMake(underlineX, underlineY, underlineW, underlineH)];
    lineView.backgroundColor =  [UIColor colorWithRed:151/255.0 green:151/255.0 blue:151/255.0 alpha:1/1.0];
    [musicItem addSubview:lineView];
    
    UIColor *normalColor = [UIColor colorWithRed:17/255.0 green:17/255.0 blue:17/255.0 alpha:1/1.0];
    UIColor *selectedColor = [UIColor colorWithRed:13/255.0 green:71/255.0 blue:193/255.0 alpha:1/1.0];
    UIButton *musicButton = [UIButton buttonWithType:UIButtonTypeCustom];
    musicButton.frame = CGRectMake(0, 0, musicItemW, musicItemH);
    [musicButton setTitle:musicName forState:UIControlStateDisabled]; //未选择
    [musicButton setTitle:musicName forState:UIControlStateSelected]; //选择并播放
    [musicButton setTitle:musicName forState:UIControlStateNormal]; //选择未播放
    
    [musicButton setTitleColor:normalColor forState:UIControlStateDisabled]; //未选择
    [musicButton setTitleColor:selectedColor forState:UIControlStateSelected]; //选择并播放
    [musicButton setTitleColor:selectedColor forState:UIControlStateNormal]; //选择未播放
    
    CGFloat textOffset = 0;
    if (![musicName isEqualToString:@"无音乐"]) {
        [musicButton setImage:[NSBundle pngImageWithName:@"play_disfilled"] forState:UIControlStateDisabled];//未选择
        [musicButton setImage:[NSBundle pngImageWithName:@"pause_outline_filled"] forState:UIControlStateSelected];//选择并播放
        [musicButton setImage:[NSBundle pngImageWithName:@"play_filled"] forState:UIControlStateNormal];//选择未播放
        textOffset = -24;
    }
    
    musicButton.titleLabel.font =  [UIFont fontWithName:@"PingFangSC-Regular" size:16];
    musicButton.tag = tag;
    [musicItem addSubview:musicButton];
    [musicButton addTarget:self action:@selector(musicButtonClick:) forControlEvents:UIControlEventTouchUpInside];
    musicButton.enabled = selected;
    if (selected) {
        self.selectedMusicBtn = musicButton;
    }
    musicButton.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    [musicButton setTitleEdgeInsets:UIEdgeInsetsMake(0, textOffset, 0, 0)];
    [musicButton setImageEdgeInsets:UIEdgeInsetsMake(0, musicItemW - 40, 0,0 )];
}

- (void)baseSetting {
    self.view.backgroundColor = [UIColor whiteColor];
    CGSize size = self.view.bounds.size;
    
    CGFloat y = [UIApplication sharedApplication].statusBarFrame.size.height;
    CGFloat h = 44;
    UILabel *title = [[UILabel alloc] initWithFrame:CGRectMake(0, y, size.width, h)];
    title.text = @"设置";
    title.textAlignment = NSTextAlignmentCenter;
    title.font = [UIFont fontWithName:@"PingFangSC-Medium" size:17];
    title.textColor = [UIColor colorWithRed:0/255.0 green:0/255.0 blue:0/255.0 alpha:1/1.0];
    [self.view addSubview:title];
    
    UIButton *closeButton = [UIButton buttonWithType:UIButtonTypeCustom];
    closeButton.frame = CGRectMake(size.width - h, y, h, h);
    [closeButton setImage:[NSBundle pngImageWithName:@"关闭"] forState:UIControlStateNormal];
    [self.view addSubview:closeButton];
    [closeButton addTarget:self action:@selector(dismiss) forControlEvents:UIControlEventTouchUpInside];
    
    
    CGFloat themeTipsX = 16;
    CGFloat themeTipsY = CGRectGetMaxY(title.frame) + 29;
    CGFloat themeTipsW = 100;
    CGFloat themeTipsH = 18;
    UILabel *themeTips = [[UILabel alloc] init];
    themeTips.frame = CGRectMake(themeTipsX, themeTipsY, themeTipsW, themeTipsH);
    themeTips.text = @"背景主题画面";
    themeTips.font = [UIFont fontWithName:@"PingFangSC-Regular" size:13];
    themeTips.textColor = [UIColor colorWithRed:38/255.0 green:38/255.0 blue:38/255.0 alpha:1/1.0];
    [self.view addSubview:themeTips];
    

    CGFloat itemX = size.width == 320 ? 2:16;
    for (int i =0; i < _themes.count; i++) {
        NSString *themeName = _themes[i];
        NSString *themeTitle = _themeNames[i];
        NSString *selectedTheme = (NSString *)[[NSUserDefaults standardUserDefaults] objectForKey:ALIVC_SelectedThemeName];
        
        //没有存储过
        if (selectedTheme.length == 0) {
            [[NSUserDefaults standardUserDefaults] setObject:_themes[0] forKey:ALIVC_SelectedThemeName];
            selectedTheme = _themes[0];
        }
        BOOL selected = NO;
        if ([themeName isEqualToString:selectedTheme]) {
            selected = YES;
        }
        [self addThemeWithPositionX:itemX tag:i themeName:themeName themeTips:themeTips themeTitle:themeTitle selected:selected];
        itemX  += 108;
    }
    
    
    
    //背景音乐
    CGFloat musicLabelX = themeTipsX;
    CGFloat musicLabelY = 320;
    UILabel *musicLabel = [[UILabel alloc] init];
    musicLabel.frame = CGRectMake(musicLabelX, musicLabelY, 52, 18);
    musicLabel.text = @"背景音乐";
    musicLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:13];
    musicLabel.textColor = [UIColor colorWithRed:38/255.0 green:38/255.0 blue:38/255.0 alpha:1/1.0];
    [self.view addSubview:musicLabel];
    
    CGFloat musicItemY = CGRectGetMaxY(musicLabel.frame) + 11;
    for (int i = 0; i < _musics.count; i++) {
        NSString *musicName = _musics[i];
        CGFloat musicItemH = 56;
        NSString *selectedMusic = (NSString *)[[NSUserDefaults standardUserDefaults] objectForKey:ALIVC_SelectedMusicName];
        
        //没有存储过
        if (selectedMusic.length == 0) {
            [[NSUserDefaults standardUserDefaults] setObject:_musics[0] forKey:ALIVC_SelectedMusicName];
            selectedMusic = _musics[0];
        }
        
        BOOL selected = NO;
        if ([selectedMusic isEqualToString:musicName]) {
            selected = YES;
        }
        [self addMusicItem:musicItemH musicItemY:musicItemY musicName:musicName selected:selected tag:i];
        musicItemY += musicItemH;
    }
}



@end
