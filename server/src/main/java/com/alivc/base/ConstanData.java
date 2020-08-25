package com.alivc.base;

import java.io.Serializable;
 
/** 
 * ClassName: ConstanData <br/> 
 * Function: TODO 系统常量. <br/> 
 * Reason:   TODO 系统常量. <br/> 
 * Date:     2018年12月13日  <br/> 
 * @author   tz 
 * @version   v0.0.1
 * @since    JDK 1.8 
 * @see       
 */
public class ConstanData implements Serializable{

	private static final long serialVersionUID = -2219320746383133223L;
	public static final Integer PAGE_SIZE = 10; 
	public static final Integer PAGE = 1; 
	 
	/**
	 * 错误消息
	 */
	public static final String ERRORMSG_SERVERERROR = "服务端错误";  
	public static final String ERRORMSG_TOKENLOSE = "token无效"; 
	public static final String ERRORMSG_NOTOKEN = "无token";  
	public static final String ERRORMSG_CODETIMEOUT = "验证码超时";  
	public static final String ERRORMSG_USERISLOCK = "用户已锁定";  
	public static final String ERRORMSG_PARM = "参数错误"; 
	
	/**
	 * 2+状态码【成功】
	 */
	/**
	 * [GET]：服务器成功返回用户请求的数据，该操作是幂等的（Idempotent）。
	 */
	public static  final String OK = "200"; 
	/**
	 * [POST/PUT/PATCH]：用户新建或修改数据成功。
	 */
	public static final String CREATED = "201"; 
	/** 
	 * [*]：表示一个请求已经进入后台排队（异步任务）
	 */
	public static final String ACCEPTED  = "202"; 
	/**
	 * [DELETE]：用户删除数据成功。
	 */
	public static final String NO_CONTENT = "204"; 
	
	/**
	 * 3+状体码【客户端异常】
	 */
	/**
	 * 参数异常，格式错误等
	 */
	public static final String PARAM_ERROR = "300"; 
	/**
	 * token过期 
	 */
	public static final String TOKEN_ERROR = "301"; 
	
	/**
	 * 4+状态码【资源或记录不存在，用户请求未授权】
	 */
	/**
	 * [POST/PUT/PATCH]：用户发出的请求有错误，服务器没有进行新建或修改数据的操作，该操作是幂等的。
	 */
	public static final String INVALID_REQUEST = "400"; 
	/**
	 * [*]：表示用户没有权限（令牌、用户名、密码错误）。
	 */
	public static final String UNAUTHORIZED = "401"; 
	/**
	 * [*] 表示用户得到授权（与401错误相对），但是访问是被禁止的。
	 */
	public static final String FORBIDDEN  = "403"; 
	/**
	 * [*]：用户发出的请求针对的是不存在的记录，服务器没有进行操作，该操作是幂等的。
	 */
	public static final String NOT_FOUND = "404"; 
	/**
	 * [GET]：用户请求的格式不可得（比如用户请求JSON格式，但是只有XML格式）。
	 */
	public static final String NOT_ACCEPTABLE = "406"; 
	/**
	 * [GET]：用户请求的资源被永久删除，且不会再得到的。	
	 * 
	 */
	public static final String GONE = "410"; 
	/**
	 * 用户已锁定
	 */
	public static final String USERISLOCK = "411"; 
	/**
	 * [POST/PUT/PATCH] 当创建一个对象时，发生一个验证错误。
	 */
	public static final String UNPROCESABLE_ENTITY = "422"; 
	
	/**
	 * 5+状态码【服务端异常】
	 */
	/**
	 * [*]：服务器发生错误，用户将无法判断发出的请求是否成功。
	 */
	public static final String INTERNAL_SERVER_ERROR = "500"; 
	
	
	/**
	 * 6+超时码【请求超时】
	 */
	public static final String TIME_OUT = "600"; 
	
	
	
}
