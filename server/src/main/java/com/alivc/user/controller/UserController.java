package com.alivc.user.controller;


import com.alibaba.fastjson.JSONObject;
import com.alivc.base.ConfigMapUtil;
import com.alivc.base.ResponseResult;
import com.alivc.base.RtcOpenAPI;
import com.alivc.user.pojo.User;
import com.alivc.user.service.ScheduledDeleteChannel;
import com.alivc.user.service.UserService;
import com.aliyuncs.rtc.model.v20180111.DescribeChannelUsersResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 生成随机用户,并返回用户信息
     *
     * @return 生成的用户信息
     */
    @RequestMapping(value = "/user/randomUser", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult randomUser(String channelId) {
        JSONObject userInfo = userService.randomUser(channelId);
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData(userInfo);
        return responseResult;

    }

    @RequestMapping(value = "/user/joinSuccess", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult joinSucess(String channelId, String userid, String userName, Integer seatIndex) {
        ResponseResult userInfo = userService.insertUser(channelId, userid, userName, seatIndex);
        ResponseResult responseResult = new ResponseResult();

        String appId = ConfigMapUtil.getValueByKey("rtc.chatroom.appId");
        ScheduledDeleteChannel.addChannel(appId, channelId);

        responseResult.setData(userInfo);
        return responseResult;
    }

    @RequestMapping(value = "/user/getSeatList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult getSeat(String channelId) {

        ResponseResult responseResult = new ResponseResult();

        String appId = ConfigMapUtil.getValueByKey("rtc.chatroom.appId");
        List<User> userList = userService.getUserList(channelId);
        List<String> userIdList = new ArrayList<>();

        for (User user : userList) {
            userIdList.add(user.getUserId());
        }

        try {
            DescribeChannelUsersResponse describeChannelUsersResponse = RtcOpenAPI.describeChannelUsers(appId, channelId);
            List<String> liveUserList = describeChannelUsersResponse.getInteractiveUserList();

            if (userList.size() == liveUserList.size()) {
                if (userList.containsAll(userIdList)) {
                    List<Map<String, String>> seatList = new ArrayList<>();

                    userList.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getSeatIndex())));
                    for (User user : userList) {
                        Map<String, String> seatInfo = new HashMap<>();
                        seatInfo.put("userId", user.getUserId());
                        seatInfo.put("seatIndex", user.getSeatIndex());
                        seatList.add(seatInfo);
                    }

                    responseResult.setData(seatList);
                    return responseResult;
                }
            }

            List<String> takeSeatUserIds = new ArrayList<>(liveUserList);
            List<String> leaveSeatUserIds = new ArrayList<>(userIdList);

            userList.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getSeatIndex())));

            List<String> existSeatIndex = new ArrayList<>();
            for (User user : userList) {
                if (liveUserList.contains(user.getUserId())) {
                    existSeatIndex.add(user.getSeatIndex());
                }
            }

            leaveSeatUserIds.removeAll(liveUserList);
            takeSeatUserIds.removeAll(userIdList);

            List<User> updateUserList = new ArrayList<>();
            for (int i = 0, takeSeatIndex = 0; i < 8 && takeSeatIndex < takeSeatUserIds.size(); i++) {
                if (!existSeatIndex.contains(String.valueOf(i))) {
                    User user = new User();
                    user.setUserId(takeSeatUserIds.get(takeSeatIndex));
                    user.setSeatIndex(String.valueOf(i));
                    updateUserList.add(user);
                    takeSeatIndex++;
                }
            }
            for (String leaveSeatUserId : leaveSeatUserIds) {
                User user = new User();
                user.setUserId(leaveSeatUserId);
                user.setSeatIndex(null);
                updateUserList.add(user);
            }

            userService.updateUserSeats(updateUserList);


            userList = userService.getUserList(channelId);

            List<Map<String, String>> seatList = new ArrayList<>();

            userList.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getSeatIndex())));
            for (User user : userList) {
                Map<String, String> seatInfo = new HashMap<>();
                seatInfo.put("userId", user.getUserId());
                seatInfo.put("seatIndex", user.getSeatIndex());
                seatList.add(seatInfo);
            }

            responseResult.setData(seatList);
            return responseResult;

        } catch (Exception e) {
            log.error("getSeatList error", e);
        }

        return responseResult;


    }

    @ResponseBody
    @RequestMapping(value = "/user/describeChannelUsers", method = RequestMethod.GET)
    public ResponseResult describeChannelUsers(String channelId) {

        ResponseResult responseResult = new ResponseResult();

        String appId = ConfigMapUtil.getValueByKey("rtc.chatroom.appId");

        try {
            DescribeChannelUsersResponse describeChannelUsersResponse = RtcOpenAPI.describeChannelUsers(appId, channelId);

            responseResult.setData(describeChannelUsersResponse);
            return responseResult;
        } catch (Exception e) {
            log.error("describeChannelUsers error", e);
        }

        return responseResult;
    }

}
