//
//  AudioRoomSeatLoadProtocal.h
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/22.
//

#ifndef AudioRoomSeatLoadProtocal_h
#define AudioRoomSeatLoadProtocal_h

@protocol AudioRoomSeatsInfoProtocal <NSObject>

- (void)getSeatList:(NSDictionary *)params
           complete:(void(^)(NSArray *seats,NSString *error))handler;



@end

#endif /* AudioRoomSeatLoadProtocal_h */
