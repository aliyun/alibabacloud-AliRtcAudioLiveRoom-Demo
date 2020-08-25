package com.alivc.base;

import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
/** 
 * ClassName: JwtUtil <br/> 
 * Function: TODO JwtUtil. <br/> 
 * Reason:   TODO JwtUtil. <br/> 
 * Date:     2018年12月13日  <br/> 
 * @author   tz 
 * @version   v0.0.1
 * @since    JDK 1.8 
 * @see       
 */
public class JwtUtil {
	private static String jianshu;

	/**
	 * 由字符串生成加密key
	 * md5加密
	 * @return
	 */
	public static SecretKey generalKey() {
		String stringKey =jianshu+"aliyun@2018";
		byte[] encodedKey = Base64.decodeBase64(stringKey);
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		return key;
	}

	/**
	 * 创建jwt
	 * 
	 * @param id
	 * @param subject
	 * @param ttlMillis
	 * @return
	 * @throws Exception
	 */
	public static String createJWT(String id, String subject, long ttlMillis) throws Exception {

		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		SecretKey key = generalKey();
		JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).signWith(signatureAlgorithm,
				key);
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}
		return builder.compact();
	}

	/**
	 * 解密jwt 刷新token时间
	 * 
	 * @param jwt
	 * @return
	 * @throws Exception
	 */
	public static Claims parseJWT(String jwt) throws Exception {
		SecretKey key = generalKey();
		Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody();
		return claims;
	}

	/**
	 *  刷新token时间
	 * @param jwt
	 * @param ttlMillis
	 * @return
	 * @throws Exception
	 */
	public static Claims refresh(String jwt, long ttlMillis) throws Exception {
		SecretKey key = generalKey();
		long nowMillis = System.currentTimeMillis();
		Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody();
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			claims.setExpiration(exp);
		}
		return claims;
	}

	public static void main(String[] args) {


		String sss = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyODg5NTEiLCJpYXQiOjE1ODQxODM4NDMsInN1YiI6IntcInVzZUlkXCI6XCIyODg5NTFcIn0iLCJleHAiOjE1ODY3NzU4NDN9.bPW4TWPjMlwlmeAc1nKqUFYVnM9cO7D4sgmBEqM-tZ8";
		try {
			Object oo = parseJWT(sss);

			System.out.println("=====");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}