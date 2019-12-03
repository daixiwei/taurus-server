package com.taurus.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * sha1
 */
public final class SHA1 {
	private static SHA1 _instance = new SHA1();
	private MessageDigest messageDigest;
	private final Logger log;

	private SHA1() {
		this.log = Logger.getLogger(getClass());
		try {
			this.messageDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			this.log.error("Could not instantiate the SHA-1 Message Digest!");
		}
	}

	public static SHA1 getInstance() {
		return _instance;
	}

	public synchronized String getHash(String s) {
		byte[] data = s.getBytes();
		this.messageDigest.update(data);

		return toHexString(this.messageDigest.digest());
	}

	public synchronized String getBase64Hash(String s) {
		byte[] data = s.getBytes();
		this.messageDigest.update(data);

		return Base64.encodeToString(this.messageDigest.digest());
	}

	private String toHexString(byte[] byteData) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < byteData.length; i++) {
			String hex = Integer.toHexString(byteData[i] & 0xFF);
			if (hex.length() == 1) {
				hex = "0" + hex;
			}
			sb.append(hex);
		}

		return sb.toString();
	}
}
