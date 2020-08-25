//
//  TagCell.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/6.
//

#import "AudioRoomTagCell.h"
#import "RTCCommon.h"
@interface AudioRoomTagCell()

@property (nonatomic,weak) UIButton *tagButton;

@end

@implementation AudioRoomTagCell

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    
    if (self)
    {
        UIButton *tagButton = [UIButton buttonWithType:UIButtonTypeCustom];
        tagButton.frame = self.bounds;
        [tagButton setBackgroundImage:[UIImage imageWithColor:[UIColor colorWithHex:0x00C1DE]] forState:UIControlStateSelected];
        [tagButton setBackgroundImage:[UIImage imageWithColor:[UIColor colorWithHex:0x4C474F]] forState:UIControlStateNormal];
        tagButton.titleLabel.font = [UIFont systemFontOfSize:12];
        tagButton.layer.cornerRadius = 12;
        tagButton.layer.masksToBounds = YES;
        tagButton.userInteractionEnabled = NO;
        [self.contentView addSubview:tagButton];
        self.tagButton = tagButton;
    }
    
    return self;;
}

- (void)layoutSubviews
{
    self.tagButton.frame = self.bounds;
}

- (void)setTagStr:(NSString *)tagStr
{
    _tagStr = tagStr;
    [self.tagButton setTitle:_tagStr forState:UIControlStateNormal];
}

- (void)setPicked:(BOOL)picked
{
    _picked = picked;
    self.tagButton.selected = picked;
}




@end
