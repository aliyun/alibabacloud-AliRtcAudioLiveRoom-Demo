//
//  SoundEffectController.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/6.
//

#import "AudioRoomSoundEffectController.h"
#import "NSBundle+RTCAudioLiveRoom.h"
#import "RTCAudioliveRoom.h"

@interface AudioRoomSoundEffectController ()
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *confirmBtn;

@property (unsafe_unretained, nonatomic) IBOutlet UISlider *laughSliderBar;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *laughAuditionButton;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *laughPublishButton;
@property (unsafe_unretained, nonatomic) IBOutlet UISlider *applauseSliderBar;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *applauseAuditonButton;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *applausePublishButton;
@property (strong, nonatomic) AudioRoomSoundEffect *effect1;
@property (strong, nonatomic) AudioRoomSoundEffect *effect2;

@end

@implementation AudioRoomSoundEffectController

- (instancetype)initWithEffect1:(AudioRoomSoundEffect *)effect1
                        effect2:(AudioRoomSoundEffect *)effect2
{
    UIStoryboard *storyboard = [NSBundle RALR_storyboard];
    self = [storyboard instantiateViewControllerWithIdentifier:@"AudioRoomSoundEffectController"];
    self.effect1 = effect1;
    self.effect2 = effect2;
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupUI];
    [self reloadData];
}

- (void)setupUI{
    [self.confirmBtn setImage:[NSBundle RALR_pngImageWithName:@"dismiss"] forState:UIControlStateNormal];
    [self.laughAuditionButton setImage:[NSBundle RALR_pngImageWithName:@"test"] forState:UIControlStateNormal];
    [self.laughPublishButton setImage:[NSBundle RALR_pngImageWithName:@"play"] forState:UIControlStateNormal];
    [self.applauseAuditonButton setImage:[NSBundle RALR_pngImageWithName:@"test"] forState:UIControlStateNormal];
    [self.applausePublishButton setImage:[NSBundle RALR_pngImageWithName:@"play"] forState:UIControlStateNormal];
}

- (void)reloadData
{
    self.laughSliderBar.value = self.effect1.volume;
    self.laughAuditionButton.selected = self.effect1.testing;
    self.laughPublishButton.selected = self.effect1.publishing;
    
    self.applauseSliderBar.value = self.effect2.volume;
    self.applauseAuditonButton.selected =self.effect2.testing;
    self.applausePublishButton.selected = self.effect2.publishing;
}

- (void)effect1PlayControl
{
    NSString *url = [self.effect1.path stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    NSInteger soundId = self.effect1.effectId;
    
    if (self.effect1.testing || self.effect1.publishing )
    {
        [[RTCAudioliveRoom sharedInstance]  playEffectSoundtWithSoundId:soundId
                                                               filePath:url
                                                                publish:self.effect1.publishing];
        [[RTCAudioliveRoom sharedInstance] setAudioEffectPlayoutVolumeWithSoundId:soundId volume:self.effect1.volume];
    } else {
        [[RTCAudioliveRoom sharedInstance] stopAudioEffectWithSoundId:soundId];
    }
}

- (void)effect2PlayControl
{
    NSString *url = [self.effect2.path stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    NSInteger soundId = self.effect2.effectId;
    if (self.effect2.testing || self.effect2.publishing )
    {
        [[RTCAudioliveRoom sharedInstance]  playEffectSoundtWithSoundId:soundId
                                                               filePath:url
                                                                publish:self.effect2.publishing];
        [[RTCAudioliveRoom sharedInstance] setAudioEffectPlayoutVolumeWithSoundId:soundId volume:self.effect2.volume];
    } else {
        [[RTCAudioliveRoom sharedInstance] stopAudioEffectWithSoundId:soundId];
    }
}

- (IBAction)laughTest:(UIButton *)sender
{
    self.effect1.testing = YES;
    self.effect1.publishing = NO;
    self.effect2.testing = NO;
    self.effect2.publishing = NO;
    
    [self reloadData];
    
    [self effect1PlayControl];
    [self effect2PlayControl];
}


- (IBAction)laughPublish:(UIButton *)sender
{
    self.effect1.testing = YES;
    self.effect1.publishing = YES;
    self.effect2.testing = NO;
    self.effect2.publishing = NO;
    
    [self reloadData];
    
    [self effect1PlayControl];
    [self effect2PlayControl];
}


- (IBAction)applauseTest:(UIButton *)sender
{
    self.effect1.testing = NO;
    self.effect1.publishing = NO;
    self.effect2.testing = YES;
    self.effect2.publishing = NO;
    
    [self reloadData];
    
    [self effect1PlayControl];
    [self effect2PlayControl];
}

- (IBAction)applausePublish:(UIButton *)sender
{
    self.effect1.testing = NO;
    self.effect1.publishing = NO;
    self.effect2.testing = YES;
    self.effect2.publishing = YES;
    
    [self reloadData];
    
    [self effect1PlayControl];
    [self effect2PlayControl];
}

- (IBAction)effect1VolumeChanged:(UISlider *)sender
{
    self.effect1.volume = sender.value;
    NSInteger soundId = self.effect1.effectId;
    [[RTCAudioliveRoom sharedInstance] setAudioEffectPlayoutVolumeWithSoundId:soundId volume:sender.value];
}


- (IBAction)effect2VolumeChanged:(UISlider *)sender
{
    self.effect2.volume = sender.value;
    NSInteger soundId = self.effect2.effectId;
    [[RTCAudioliveRoom sharedInstance] setAudioEffectPlayoutVolumeWithSoundId:soundId
                                                                       volume:sender.value];
}

- (IBAction)dissmiss:(id)sender
{
    self.effect1.testing = NO;
    self.effect1.publishing = NO;
    self.effect2.testing = NO;
    self.effect2.publishing = NO;
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end
