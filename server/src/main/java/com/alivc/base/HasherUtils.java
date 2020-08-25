package com.alivc.base;


import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import org.apache.commons.codec.binary.Base64;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
/** 
 * ClassName: HasherUtils <br/> 
 * Function: TODO HasherUtils. <br/> 
 * Reason:   TODO HasherUtils. <br/> 
 * Date:     2018年12月13日  <br/> 
 * @author   tz 
 * @version   v0.0.1
 * @since    JDK 1.8 
 * @see       
 */
public class HasherUtils {
	public static final Integer DEFAULT_ITERATIONS = 100000;
	public static final String ALGORITHM = "pbkdf2_sha256";

	public HasherUtils() {
	}

	public static String getEncodedHash(String password, String salt, int iterations) {
		// Returns only the last part of whole encoded password
		SecretKeyFactory keyFactory = null;
		try {
			keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Could NOT retrieve PBKDF2WithHmacSHA256 algorithm");
			System.exit(1);
		}
		KeySpec keySpec = new PBEKeySpec(
				password.toCharArray(), salt.getBytes(Charset.forName("UTF-8")), iterations, 256);
		SecretKey secret = null;
		try {
			secret = keyFactory.generateSecret(keySpec);
		} catch (InvalidKeySpecException e) {
			System.out.println("Could NOT generate secret key");
			e.printStackTrace();
		}

		byte[] rawHash = secret.getEncoded();

		byte[] hashBase64 = Base64.encodeBase64(rawHash);

		return new String(hashBase64);
	}

	/**
	 * make salt 
	 * @return String
	 */
	private static String getsalt() {
		int length = 12;
		Random rand = new Random();
		char[] rs = new char[length];
		for (int i = 0; i < length; i++) {
			int t = rand.nextInt(3);
			if (t == 0) {
				rs[i] = (char) (rand.nextInt(10) + 48);
			} else if (t == 1) {
				rs[i] = (char) (rand.nextInt(26) + 65);
			} else {
				rs[i] = (char) (rand.nextInt(26) + 97);
			}
		}
		return new String(rs);
	}

	/**
	 * rand salt
	 * iterations is default 20000
	 * @param password
	 * @return
	 */
	public static String encode(String password) {
		return encode(password, getsalt());
	}

	/**
	 * rand salt
	 * @param password
	 * @return
	 */
	public String encode(String password, int iterations) {
		return encode(password, getsalt(), iterations);
	}

	/**
	 * iterations is default 20000
	 * @param password
	 * @param salt
	 * @return
	 */
	public static String encode(String password, String salt) {
		return encode(password, salt, DEFAULT_ITERATIONS);
	}

	/**
	 * 
	 * @param password
	 * @param salt
	 * @param iterations
	 * @return
	 */
	public static String encode(String password, String salt, int iterations) {
		// returns hashed password, along with algorithm, number of iterations and salt
		String hash = getEncodedHash(password, salt, iterations);
		return String.format("%s$%d$%s$%s", ALGORITHM, iterations, salt, hash);
	}

	public static boolean checkPassword(String password, String hashedPassword) {
		// hashedPassword consist of: ALGORITHM, ITERATIONS_NUMBER, SALT and
		// HASH; parts are joined with dollar character ("$")
		String[] parts = hashedPassword.split("\\$");
		int length = 4;
		if (parts.length != length) {
			// wrong hash format
			return false;
		}
		Integer iterations = Integer.parseInt(parts[1]);
		String salt = parts[2];
		String hash = encode(password, salt, iterations);

		return hash.equals(hashedPassword);
	}

}
