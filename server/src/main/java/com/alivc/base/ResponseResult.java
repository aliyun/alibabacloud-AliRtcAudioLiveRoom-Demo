package com.alivc.base;

import java.io.Serializable;
import java.util.UUID;



/** 
 * ClassName: ResponseResult <br/> 
 * Function: TODO 返回数据模型. <br/> 
 * Reason:   TODO 用于自定义的数据模型. <br/> 
 * Date:     2018年11月10日  <br/> 
 * @author   tz 
 * @version   v0.0.1
 * @since    JDK 1.8 
 * @see       
 */ 
public class ResponseResult implements Serializable {

	 
	private static final long serialVersionUID = -2305573436444343979L;
	/**
	 * 请求状态
	 */
	private String result = "";
	
	/**
	 * 请求id
	 */
	private String requestId = "";

	/**
	 * 信息
	 */
	private String message = "";
	/**
	 * code
	 */
	private String code = "";

	/**
	 * 数据
	 */
	private Object data;

	 
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	
	public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
	 * 生产响应信息
	 * @param msg
	 *            响应消息
	 * @param data
	 *            响应数据
	 */
	public void response(String result,  String msg, Object data) {
		this.result = result;
		this.data = data;
		this.message = msg;
	}


	/**
	 * 构造函数，基于需求每次给result，code
	 */
	 public ResponseResult(String requestId){
	        this.result = "true";
	        this.code = ConstanData.OK;
	        this.requestId = requestId;
	 }

	public ResponseResult(){
		this.result = "true";
		this.code = ConstanData.OK;
	}
}
