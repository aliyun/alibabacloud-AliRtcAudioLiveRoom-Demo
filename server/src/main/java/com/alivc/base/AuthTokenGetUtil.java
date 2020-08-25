package com.alivc.base;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.Claims;
/** 
 * ClassName: AuthTokenGetUtil <br/> 
 * Function: TODO authtoken util. <br/> 
 * Reason:   TODO authtoken util. <br/> 
 * Date:     2018年12月13日  <br/> 
 * @author   tz 
 * @version   v0.0.1
 * @since    JDK 1.8 
 * @see       
 */
public class AuthTokenGetUtil {
	private static final  Logger LOG=LoggerFactory.getLogger(AuthTokenGetUtil.class);
	private static final String AUTHHEADERNAME="Authorization";
	private static final String USER_ID_NAME="useId";
	/**
	 * 获取请求头中的token
	 * @param request 用户请求
	 * @return 用户认证后的token
	 */
	public static String getAuthTokenFromRequest(HttpServletRequest request) {
		String token = null;
		if(request.getHeader(AUTHHEADERNAME) != null){
			token = request.getHeader(AUTHHEADERNAME).substring(4);
		}
		return token;
	}
	/**
	 * 从请求中获取用户登录id
	 * @param request 请求对象
	 * @return  用户id
	 */
	public static String getUserIdFormRequest(HttpServletRequest request) {
		return getUserIdFormToken(getAuthTokenFromRequest(request));
	}
	/**
	 * 获取token中的用户id
	 * @param token 用户登录信息的加密token
	 * @return 用户id
	 */
	public static String getUserIdFormToken(String token) {
		if(StringUtils.isNotBlank(token)){
			try {
				Claims jwt = JwtUtil.parseJWT(token);
				String subject = jwt.getSubject();
				JSONObject json=new JSONObject(subject);
				 return json.getString(USER_ID_NAME);
				}catch(Exception e) {
					LOG.error("--获取token中的用户id出现异常:{}",token,e);
			}
		}
		return null;
	}
}
