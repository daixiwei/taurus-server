package com.taurus.core.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * base64
 */
public final class Base64 {
	private static final char[]	CA	= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	private static final int[]	IA	= new int[256];
	
	static {
		Arrays.fill(IA, -1);
		int i = 0;
		for (int iS = CA.length; i < iS; i++) {
			IA[CA[i]] = i;
		}
		IA[61] = 0;
	}
	
	private static final char[] encodeToChar(byte[] sArr, boolean lineSep) {
		int sLen = sArr != null ? sArr.length : 0;
		if (sLen == 0) {
			return new char[0];
		}
		int eLen = sLen / 3 * 3;
		int cCnt = (sLen - 1) / 3 + 1 << 2;
		int dLen = cCnt + (lineSep ? (cCnt - 1) / 76 << 1 : 0);
		char[] dArr = new char[dLen];
		
		int s = 0;
		int d = 0;
		for (int cc = 0; s < eLen;) {
			int i = (sArr[(s++)] & 0xFF) << 16 | (sArr[(s++)] & 0xFF) << 8 | sArr[(s++)] & 0xFF;
			
			dArr[(d++)] = CA[(i >>> 18 & 0x3F)];
			dArr[(d++)] = CA[(i >>> 12 & 0x3F)];
			dArr[(d++)] = CA[(i >>> 6 & 0x3F)];
			dArr[(d++)] = CA[(i & 0x3F)];
			if (lineSep) {
				cc++;
				if ((cc == 19) && (d < dLen - 2)) {
					dArr[(d++)] = '\r';
					dArr[(d++)] = '\n';
					cc = 0;
				}
			}
		}
		int left = sLen - eLen;
		if (left > 0) {
			int i = (sArr[eLen] & 0xFF) << 10 | (left == 2 ? (sArr[(sLen - 1)] & 0xFF) << 2 : 0);
			
			dArr[(dLen - 4)] = CA[(i >> 12)];
			dArr[(dLen - 3)] = CA[(i >>> 6 & 0x3F)];
			dArr[(dLen - 2)] = (left == 2 ? CA[(i & 0x3F)] : '=');
			dArr[(dLen - 1)] = '=';
		}
		return dArr;
	}
	
	/**
     * Encodes all bytes from the specified byte array into a newly-allocated
     * byte array using the {@link Base64} encoding scheme. The returned byte
     * array is of the length of the resulting bytes.
     *
     * @param   src the byte array to encode
     * @return  A newly-allocated byte array containing the resulting encoded bytes.
     */
	private static final String encodeToString(byte[] src, boolean lineSep) {
		return new String(encodeToChar(src, lineSep));
	}
	
	/**
     * Encodes all bytes from the specified byte array into a newly-allocated
     * byte array using the {@link Base64} encoding scheme. The returned byte
     * array is of the length of the resulting bytes.
     *
     * @param   src the byte array to encode
     * @return  A newly-allocated byte array containing the resulting encoded bytes.
     */
	public static final byte[] encode(String src) {
		try {
			return StringUtil.getBytes(encodeToString(StringUtil.getBytes(src), false));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
     * Encodes all bytes from the specified byte array into a newly-allocated
     * byte array using the {@link Base64} encoding scheme. The returned byte
     * array is of the length of the resulting bytes.
     *
     * @param   src the byte array to encode
     * @return  A newly-allocated byte array containing the resulting encoded bytes.
     */
	public static final byte[] encode(byte[] src) {
		try {
			return StringUtil.getBytes(encodeToString(src, false));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
     * Encodes all bytes from the specified byte array into a newly-allocated
     * byte array using the {@link Base64} encoding scheme. The returned byte
     * array is of the length of the resulting bytes.
     *
     * @param   src the byte array to encode
     * @return  A newly-allocated byte array containing the resulting encoded bytes.
     */
	public static final String encodeToString(String src) {
		try {
			return encodeToString(StringUtil.getBytes(src), false);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
     * Encodes all bytes from the specified byte array into a newly-allocated
     * byte array using the {@link Base64} encoding scheme. The returned byte
     * array is of the length of the resulting bytes.
     *
     * @param   src the byte array to encode
     * @return  A newly-allocated byte array containing the resulting encoded bytes.
     */
	public static final String encodeToString(byte[] src) {
		return encodeToString(src, false);
	}
	
	/**
     * Decodes all bytes from the input byte array using the {@link Base64}
     * encoding scheme, writing the results into a newly-allocated output
     * byte array. The returned byte array is of the length of the resulting
     * bytes.
     *
     * @param   src the byte array to decode
     * @return  A newly-allocated byte array containing the decoded bytes.
     */
	public static final String decodeToString(byte[] src) {
		try {
			return StringUtil.getString(decode(src));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
     * Decodes all bytes from the input byte array using the {@link Base64}
     * encoding scheme, writing the results into a newly-allocated output
     * byte array. The returned byte array is of the length of the resulting
     * bytes.
     *
     * @param   src the byte array to decode
     * @return  A newly-allocated byte array containing the decoded bytes.
     */
	public static final String decodeToString(String src) {
		try {
			return StringUtil.getString(decode(src));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
     * Decodes all bytes from the input byte array using the {@link Base64}
     * encoding scheme, writing the results into a newly-allocated output
     * byte array. The returned byte array is of the length of the resulting
     * bytes.
     *
     * @param   src the byte array to decode
     * @return  A newly-allocated byte array containing the decoded bytes.
     */
	public static final byte[] decode(byte[] src) {
		try {
			return decode(StringUtil.getString(src));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
     * Decodes all bytes from the input byte array using the {@link Base64}
     * encoding scheme, writing the results into a newly-allocated output
     * byte array. The returned byte array is of the length of the resulting
     * bytes.
     *
     * @param   src the byte array to decode
     * @return  A newly-allocated byte array containing the decoded bytes.
     */
	public static final byte[] decode(String src) {
		int sLen = src != null ? src.length() : 0;
		if (sLen == 0) {
			return new byte[0];
		}
		int sepCnt = 0;
		for (int i = 0; i < sLen; i++) {
			if (IA[src.charAt(i)] < 0) {
				sepCnt++;
			}
		}
		if ((sLen - sepCnt) % 4 != 0) {
			return null;
		}
		int pad = 0;
		for (int i = sLen; (i > 1) && (IA[src.charAt(--i)] <= 0);) {
			if (src.charAt(i) == '=') {
				pad++;
			}
		}
		int len = ((sLen - sepCnt) * 6 >> 3) - pad;
		
		byte[] dArr = new byte[len];
		
		int s = 0;
		for (int d = 0; d < len;) {
			int i = 0;
			for (int j = 0; j < 4; j++) {
				int c = IA[src.charAt(s++)];
				if (c >= 0) {
					i |= c << 18 - j * 6;
				} else {
					j--;
				}
			}
			dArr[(d++)] = ((byte) (i >> 16));
			if (d < len) {
				dArr[(d++)] = ((byte) (i >> 8));
				if (d < len) {
					dArr[(d++)] = ((byte) i);
				}
			}
		}
		return dArr;
	}
}
