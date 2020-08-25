//
//  TagCell.h
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/6.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AudioRoomTagCell : UICollectionViewCell

@property (copy,nonatomic) NSString *tagStr;

@property (assign,nonatomic) BOOL picked;

@end

NS_ASSUME_NONNULL_END
