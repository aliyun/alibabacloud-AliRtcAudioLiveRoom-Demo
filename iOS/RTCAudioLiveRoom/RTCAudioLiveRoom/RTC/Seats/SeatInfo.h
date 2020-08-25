//
//  Seat.h
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/6.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface SeatInfo : NSObject

@property (copy, nonatomic, nullable) NSString *userId; //用户的id
@property (assign, nonatomic) NSInteger seatIndex;   //座位号
@property (copy, nonatomic) NSString *userName;      //昵称
@property (assign, nonatomic) BOOL muteMic;            //是否静音
@property (assign, nonatomic) BOOL speaking;         //是否在说话
@property (copy, nonatomic) NSString *icon;          //座位头像

@end

NS_ASSUME_NONNULL_END
