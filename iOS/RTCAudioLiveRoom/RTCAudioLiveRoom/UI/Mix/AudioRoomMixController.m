//
//  MixController.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/6.
//

#import "AudioRoomMixController.h"
#import "AudioRoomTagCell.h"
#import "NSBundle+RTCAudioLiveRoom.h"
#import "RTCAudioliveRoom.h"

static  NSString *ReuseId = @"TagCell";

@interface AudioRoomMixController () <UICollectionViewDelegate,UICollectionViewDataSource,UICollectionViewDelegateFlowLayout>

@property (strong, nonatomic) NSArray *reverbModes;
@property (strong, nonatomic) NSArray *voiceChangeModes;
@property (assign, nonatomic) NSInteger selectedReverbModeIndex;
@property (assign, nonatomic) NSInteger selectedVoiceChangeModeIndex;
@property (strong, nonatomic) AudioRoomMixSetting *mixSetting;
@property (unsafe_unretained, nonatomic) IBOutlet UICollectionView *collectionView;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *confirmButton;
@property (unsafe_unretained, nonatomic) IBOutlet UISwitch *earbackSwitch;
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *earBackLabel;
@property (weak, nonatomic) UILabel *changeVoiceLabel;

@end

@implementation AudioRoomMixController

#pragma mark - system methods

- (instancetype)initWithMixSetting:(AudioRoomMixSetting *)mixSetting {
    UIStoryboard *storyboard = [NSBundle RALR_storyboard];
    self = [storyboard instantiateViewControllerWithIdentifier:@"AudioRoomMixController"];
    self.mixSetting = mixSetting;
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupUI];
}

- (void)viewWillLayoutSubviews {
    [super viewWillLayoutSubviews];
    [self setupChangeVoiceLabel];
}

#pragma mark - private methods

- (void)setupUI
{
    self.selectedReverbModeIndex = self.mixSetting.mixType;
    self.selectedVoiceChangeModeIndex = self.mixSetting.voiceChangeType;
    self.earbackSwitch.on = self.mixSetting.earBack;
    self.earBackLabel.text =self.earbackSwitch.on ? @"开启":@"关闭";
    [self.confirmButton setImage:[NSBundle RALR_pngImageWithName:@"dismiss"] forState:UIControlStateNormal];
    self.collectionView.delegate = self;
    self.collectionView.dataSource = self;
    [self.collectionView registerClass:[AudioRoomTagCell class] forCellWithReuseIdentifier:ReuseId];
    UICollectionViewFlowLayout *layout = (UICollectionViewFlowLayout *)self.collectionView.collectionViewLayout;
    layout.minimumInteritemSpacing = 5;
    layout.sectionInset = UIEdgeInsetsMake(0, 0, 18, 0);
}

- (void)setupChangeVoiceLabel {
    NSIndexPath *offIndex = [NSIndexPath indexPathForItem:0 inSection:1];
    UICollectionViewLayoutAttributes *attributes = [self.collectionView layoutAttributesForItemAtIndexPath:offIndex];
    CGRect labelframe = [self.collectionView convertRect:attributes.frame toView:self.view];
    labelframe.origin.x = labelframe.origin.x - 50;
    self.changeVoiceLabel.frame = labelframe;
}

#pragma mark - collectionView delegate
- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
    return 2;
}
- (NSInteger)collectionView:(UICollectionView *)collectionView
     numberOfItemsInSection:(NSInteger)section
{
    if (section == 0) {
        return self.reverbModes.count;
    } else {
        return self.voiceChangeModes.count;
    }
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    AudioRoomTagCell *tagCell = [collectionView dequeueReusableCellWithReuseIdentifier:ReuseId forIndexPath:indexPath];
    if (indexPath.section == 0) {
        tagCell.tagStr = self.reverbModes[indexPath.item];
        tagCell.picked = indexPath.item == self.selectedReverbModeIndex;
    } else {
        tagCell.tagStr = self.voiceChangeModes[indexPath.item];
        tagCell.picked = indexPath.item == self.selectedVoiceChangeModeIndex;
    }
    return tagCell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView
                  layout:(UICollectionViewLayout *)collectionViewLayout
  sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *content;
    
    if (indexPath.section == 0)
    {
        content = self.reverbModes[indexPath.item];
    } else {
        content = self.voiceChangeModes[indexPath.item];
    }
    
    return CGSizeMake(content.length * 12 + 16,24);
}

- (void)reverbModeClicked:(UICollectionView * _Nonnull)collectionView
                indexPath:(NSIndexPath * _Nonnull)indexPath resetVoice:(BOOL)resetVoice {
    if (self.selectedReverbModeIndex!= indexPath.item)
    {
        
        AudioRoomTagCell * cell = (AudioRoomTagCell *)[collectionView cellForItemAtIndexPath:[NSIndexPath indexPathForItem:self.selectedReverbModeIndex inSection:0]];
        cell.picked = NO;
        self.selectedReverbModeIndex = indexPath.item;
        AudioRoomTagCell * selectedCell = (AudioRoomTagCell *)[collectionView cellForItemAtIndexPath:indexPath];
        selectedCell.picked = YES;
        self.mixSetting.mixType = self.selectedReverbModeIndex;
        
        NSIndexPath *offIndex = [NSIndexPath indexPathForItem:0 inSection:1];
        if (resetVoice)
        {
            [self voiceChangeClicked:self.collectionView
                          indexPath:offIndex resetReverb:NO];
            [[RTCAudioliveRoom sharedInstance] setAudioEffectReverbMode:self.selectedReverbModeIndex];
        }
        
    }
}

- (void)voiceChangeClicked:(UICollectionView * _Nonnull)collectionView
                 indexPath:(NSIndexPath * _Nonnull)indexPath  resetReverb:(BOOL)resetReverb{
    if (self.selectedVoiceChangeModeIndex!= indexPath.item)
    {
        AudioRoomTagCell * cell = (AudioRoomTagCell *)[collectionView cellForItemAtIndexPath:[NSIndexPath indexPathForItem:self.selectedVoiceChangeModeIndex inSection:1]];
        cell.picked = NO;
        self.selectedVoiceChangeModeIndex = indexPath.item;
        AudioRoomTagCell * selectedCell = (AudioRoomTagCell *)[collectionView cellForItemAtIndexPath:indexPath];
        selectedCell.picked = YES;
        self.mixSetting.voiceChangeType = self.selectedVoiceChangeModeIndex;
        
        NSIndexPath *offIndex = [NSIndexPath indexPathForItem:0 inSection:0];
        
        if (resetReverb)
        {
            [self reverbModeClicked:self.collectionView
                          indexPath:offIndex resetVoice:NO];
            [[RTCAudioliveRoom sharedInstance] setAudioEffectVoiceChangerMode:self.selectedVoiceChangeModeIndex];
        }
    }
}

- (void)collectionView:(UICollectionView *)collectionView
didSelectItemAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.section == 0) {
        [self reverbModeClicked:collectionView indexPath:indexPath resetVoice:YES];
    } else {
        [self voiceChangeClicked:collectionView indexPath:indexPath resetReverb:YES];
    }
}

#pragma mark - actions

- (IBAction)confirm:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)close:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)earback:(UISwitch *)sender
{
    self.mixSetting.earBack = sender.on;
    if (sender.on) {
        self.earBackLabel.text = @"开启";
    }else {
        self.earBackLabel.text = @"关闭";
    }
    
    [[RTCAudioliveRoom sharedInstance] enableEarBack:sender.on];
}

#pragma mark - setter & getter

- (NSArray *)reverbModes
{
    if (!_reverbModes)
    {
        _reverbModes = @[@"无效果",@"人声I",@"人声II",@"澡堂",@"明亮小房间",@"黑暗小房间",@"中等房间",@"大房间",@"教堂走廊",@"大教堂"];
    }
    
    return _reverbModes;
}

- (NSArray *)voiceChangeModes
{
    if (!_voiceChangeModes)
    {
        _voiceChangeModes = @[@"关闭",@"老人",@"男孩",@"女孩",@"机器人",@"大魔王",@"KTV",@"回声"];
    }
    return _voiceChangeModes;
}

- (UILabel *)changeVoiceLabel {
    if (!_changeVoiceLabel) {
        UILabel *label  = [[UILabel alloc] initWithFrame:CGRectZero];
        label.text = @"变声";
        label.textColor = [UIColor whiteColor];
        [label sizeToFit];
        label.font = [UIFont systemFontOfSize:12];
        _changeVoiceLabel = label;
        [self.view addSubview:label];
    }
    return _changeVoiceLabel;;
}
@end
