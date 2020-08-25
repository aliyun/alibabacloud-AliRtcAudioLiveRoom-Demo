//
//  Seat.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/6.
//

#import "SeatInfo.h"

@interface SeatInfo ()

@end

@implementation SeatInfo

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.seatIndex = -1;
        self.muteMic = NO;
        self.speaking = NO;
    }
    return self;
}
- (NSString *)icon
{
    if (_userId.length == 0)
    {
        _icon = @"empty";
    }else {
        _icon = [NSString stringWithFormat:@"%d",(int)_seatIndex + 1];
    }
    return _icon;
}

- (void)setUserId:(NSString *)userId
{
    _userId = userId;
    
    if (userId.length==0)
    {
        _icon = @"empty";
        _userName = [NSString stringWithFormat:@"%d号麦",(int)_seatIndex + 1];
        _speaking = NO;
        _muteMic = NO;
    }else {
        _icon = [NSString stringWithFormat:@"%d",(int)_seatIndex + 1];
    }
}
@end
