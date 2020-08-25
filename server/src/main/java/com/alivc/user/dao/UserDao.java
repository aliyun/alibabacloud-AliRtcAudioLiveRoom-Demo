package com.alivc.user.dao;

import com.alivc.user.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * ClassName: UserDao <br/>
 * Function: TODO 用户的dao层. <br/>
 * Reason:   TODO 用户的dao层. <br/>
 * Date:     2018年11月10日  <br/>
 *
 * @author tz
 * @version v0.0.1
 * @see
 * @since JDK 1.8
 */
@Mapper
public interface UserDao {

    void insertUser(@Param("channelId") String channelId, @Param("userid") String userid, @Param("userName") String userName, @Param("seatIndex") Integer seatIndex);

    void updateUser(@Param("channelId") String channelId, @Param("userid") String userid, @Param("userName") String userName, @Param("seatIndex") Integer seatIndex);

    List<User> getUserList(@Param("channelId") String channelId);

    void updateUserSeatIndex(@Param("user") User user);
}
