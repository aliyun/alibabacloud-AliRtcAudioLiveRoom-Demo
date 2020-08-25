package com.alivc.base;  

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
/** 
 * ClassName:ConfigMapUtil <br/> 
 * Function: TODO 配置公共参数 <br/> 
 * Reason:   TODO 配置公共参数. <br/> 
 * Date:     2019年3月26日 下午3:13:07 <br/> 
 * @author   176xiangkou 
 * @version   
 * @since    JDK 1.8 
 * @see       
 */
public class ConfigMapUtil {
	private static Map<String, String> map = new HashMap<String, String>();
	
	    static {
	        try {
	            //读取文件流
	            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
	            //转变为字符流
	            InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream,"utf-8");
	            //创建 Properties 对象
	            Properties properties = new Properties();
	           // prop.load(new InputStreamReader(in, "utf-8"));
	            //加载字符流
	            properties.load(inputStreamReader);
	            //获取所有key
	            Enumeration enumeration = properties.propertyNames();
	            while (enumeration.hasMoreElements()) {
	                //遍历key
	                String key = (String) enumeration.nextElement();
	                //根据key取值
	                String value = properties.getProperty(key);
	                //放入map中
	                map.put(key, value);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    public static String getValueByKey(String key) {
	        return map.get(key);
	    }
	
	    public static Map<String, String> getMap() {
	        return map;
	    }
	
	    public static void setMap(Map<String, String> map) {
	        ConfigMapUtil.map = map;
	    }
	
	}