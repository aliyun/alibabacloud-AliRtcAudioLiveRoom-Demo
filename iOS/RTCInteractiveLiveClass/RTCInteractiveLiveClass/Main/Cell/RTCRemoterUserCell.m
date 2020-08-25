//
//  RTCRemoterUserCell.m
//  LectureHall
//
//  Created by Aliyun on 2020/5/25.
//  Copyright © 2020 alibaba. All rights reserved.
//

#import "RTCRemoterUserCell.h"

@implementation RTCRemoterUserCell
{
    AliRenderView *viewRemote;
}

- (instancetype)initWithFrame:(CGRect)frame {
    
    self = [super initWithFrame:frame];
    if (self) {
        //设置远端流界面
        CGRect rc  = CGRectMake(0, 0, 90, 90);
        viewRemote = [[AliRenderView alloc] initWithFrame:rc];
        self.backgroundColor = [UIColor clearColor];      
    }
    return self;
}

- (void)updateUserRenderview:(AliRenderView *)view {
    view.backgroundColor = [UIColor clearColor];
    view.frame = CGRectMake(0, 0, 90, 90);
    view.subviews.firstObject.frame = CGRectMake(0, 0, 90, 90);
    viewRemote = view;
    [self addSubview:viewRemote];
}

@end
