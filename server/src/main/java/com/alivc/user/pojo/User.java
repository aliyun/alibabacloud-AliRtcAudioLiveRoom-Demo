package com.alivc.user.pojo;

import lombok.Data;

@Data
public class User {

    private String channelId;

    private String userId;
    /**
     * 用户昵称
     */
    private String userName;

    private String seatIndex;


}
