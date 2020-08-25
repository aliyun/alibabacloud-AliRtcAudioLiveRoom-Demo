//
//  BackgroundMusicController.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/6.
//

#import "AudioRoomBackgroundMusicController.h"
#import "NSBundle+RTCAudioLiveRoom.h"
#import "RTCAudioliveRoom.h"

@interface AudioRoomBackgroundMusicController ()

@property (unsafe_unretained, nonatomic) IBOutlet UISlider *sliderBar;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *confirmBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *auditionBtn; //试听按钮
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *publishBtn;  //推送按钮

@property (nonatomic,strong) AudioRoomBackgroundMusic *music;

@end

@implementation AudioRoomBackgroundMusicController


- (instancetype)initWithMusic:(AudioRoomBackgroundMusic *)music
{
    UIStoryboard *storyboard = [NSBundle RALR_storyboard];
    self = [storyboard instantiateViewControllerWithIdentifier:@"AudioRoomBackgroundMusicController"];
    self.music = music;
    
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self setupUI];
    [self reloadData];
}

- (void)setupUI
{
    [self.confirmBtn setImage:[NSBundle RALR_pngImageWithName:@"dismiss"] forState:UIControlStateNormal];
    [self.auditionBtn setImage:[NSBundle RALR_pngImageWithName:@"test"] forState:UIControlStateNormal];
    [self.auditionBtn setImage:[NSBundle RALR_pngImageWithName:@"pause"] forState:UIControlStateSelected];
    [self.publishBtn setImage:[NSBundle RALR_pngImageWithName:@"play"] forState:UIControlStateNormal];
    [self.publishBtn setImage:[NSBundle RALR_pngImageWithName:@"pause"] forState:UIControlStateSelected];
}

- (void)reloadData
{
    self.sliderBar.value = self.music.volume;
    self.auditionBtn.selected =  self.music.testing;
    self.publishBtn.selected = self.music.publishing;
}

- (void)playControl
{
    NSString *url = [self.music.path stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    if (self.music.testing|| self.music.publishing )
    {
        [[RTCAudioliveRoom sharedInstance] startAudioAccompanyWithFile:url
                                                               publish:self.music.publishing ];
        [[RTCAudioliveRoom sharedInstance] setAudioAccompanyVolume: self.music.volume];
    } else {
        [[RTCAudioliveRoom sharedInstance] stopAudioAccompany];
    }
}

- (IBAction)dissmiss:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)confirm:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)test:(UIButton *)sender
{
    self.music.testing = !self.music.testing;
    self.music.publishing = NO;
    [self reloadData];
    [self playControl];
}
- (IBAction)publish:(UIButton *)sender
{
    self.music.testing = NO;
    self.music.publishing = !self.music.publishing;
    [self reloadData];
    [self playControl];
}

- (IBAction)chaneVolum:(UISlider *)sender
{
    self.music.volume = sender.value;
    [[RTCAudioliveRoom sharedInstance] setAudioAccompanyVolume:self.music.volume];
}


@end
