//
//  RTCRemoterUserCell.h
//  LectureHall
//
//  Created by Aliyun on 2020/5/25.
//  Copyright © 2020 alibaba. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AliRTCSdk/AliRTCSdk.h>

NS_ASSUME_NONNULL_BEGIN

@interface RTCRemoterUserCell : UICollectionViewCell

- (void)updateUserRenderview:(AliRenderView *)view;

@end

NS_ASSUME_NONNULL_END
