package com.alivc.user.service;

import com.alibaba.fastjson.JSONObject;
import com.alivc.base.ResponseResult;
import com.alivc.user.pojo.User;

import java.util.List;

/** 
 * ClassName: UserService <br/> 
 * Function: TODO 用户service层. <br/> 
 * Reason:   TODO 用于用户相关功能的接口. <br/> 
 * Date:     2018年11月10日  <br/> 
 * @author   tz 
 * @version   v0.0.1
 * @since    JDK 1.8 
 * @see       
 */
public interface UserService {
	
	/**
	 * 新增随机用户
	 * @return ResponseResult
	 */
	JSONObject randomUser(String channelId);

	ResponseResult updateUser(String channelId, String userid, String userName, Integer seatIndex);

	ResponseResult insertUser(String channelId, String userid, String userName, Integer seatIndex);

	List<User> getUserList(String channelId);

    void updateUserSeats(List<User> updateUserList);
}
