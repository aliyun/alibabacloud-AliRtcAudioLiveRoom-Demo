package com.alivc.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alivc.base.RandomString;
import com.alivc.base.ResponseResult;
import com.alivc.base.RtcOpenAPI;
import com.alivc.user.dao.UserDao;
import com.alivc.user.pojo.User;
import com.alivc.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {


    @Resource
    private UserDao userDao;


    /**
     * 生成随机用户
     *
     * @return 用户信息
     */
    @Override
    public JSONObject randomUser(String channelId) {

        if (StringUtils.isBlank(channelId)) {
            channelId = RandomStringUtils.randomNumeric(5);
        }

        try {
            JSONObject rtcAuth = RtcOpenAPI.createToken(channelId, UUID.randomUUID().toString());
            rtcAuth.put("userName", RandomString.getRandomName());
            return rtcAuth;
        } catch (Exception e) {
            log.error("create rtc auth error", e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ResponseResult updateUser(String channelId, String userid, String userName, Integer seatIndex) {
        userDao.updateUser(channelId, userid, userName, seatIndex);
        return null;
    }

    @Override
    public ResponseResult insertUser(String channelId, String userid, String userName, Integer seatIndex) {
        userDao.insertUser(channelId, userid, userName, seatIndex);

        return null;
    }

    @Override
    public List<User> getUserList(String channelId) {
        List<User> userList = userDao.getUserList(channelId);

        return userList;
    }

    @Override
    public void updateUserSeats(List<User> updateUserList) {
        for (User user : updateUserList) {
            userDao.updateUserSeatIndex(user);
        }
    }

}
