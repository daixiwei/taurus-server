package com.taurus.core.util;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * string tools	
 * @author daixiwei	daixiwei15@126.com
 */
public class StringUtil {
	
	public static final String UTF_8 = "UTF-8";
	/**
	 * 空字符
	 */
	public static final String Empty = "";
	
	/**
	 * 流数据转换为字符串  UTF-8
	 * @param bytes
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getString(byte[] bytes) throws UnsupportedEncodingException {
		return new String(bytes,UTF_8);
	}
	
	public static byte[] getBytes(String str) throws UnsupportedEncodingException {
		return str==null?null:str.getBytes(UTF_8);
	}
	
	/**
	 * 获得非空
	 * @param str
	 * @return
	 */
	public static String getStr(String str) {
		if (isEmpty(str)) return StringUtil.Empty;
		return str;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static int getBytesLength(String str) {
		if (isEmpty(str)) {
			return 0;
		}
		return str.getBytes().length;
	}

	/**
	 * 字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if ((str == null) || (Empty.equals(str.trim())) || str.length() == 0) {
			return true;
		}

		return false;
	}
	
	/**
	 * 字符串不为空
	 * @param str
	 * @return
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 首字母大写
	 * @param str
	 * @return
	 */
	public static String capitalize(final String str) {
        final int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuffer(strLen).append(Character.toTitleCase(str.charAt(0))).append(str.substring(1)).toString();
    }
	
	/**
	 * 首字母小写
	 * @param str
	 * @return
	 */
	public static String uncapitalize(final String str) {
        final int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuffer(strLen).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1)).toString();
    }
	
	public static String getCurrentTime() {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
		String time = f.format(new Date());
		return time;
	}

	public static Object removeEndChar(String object, String cha) {
		while ((isNotEmpty(object)) && (object.endsWith(cha))) {
			object = object.substring(0, object.length() - 1);
		}
		return object;
	}

	/**
	 * 获取正则表达式转换后的字符串
	 * @param str
	 * @param regex	正则表达式
	 * @return
	 */
	public static String getRegexStr(String str, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(1);
		}

		return null;
	}
	
	/**
	 * 获取正则表达式转换后的字符串数组
	 * @param str
	 * @param regex	正则表达式
	 * @return
	 */
	public static List<String> getRegexStrs(String str, String regex) {
		List<String> list = new ArrayList<String>();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		while (m.find()) {
			list.add(m.group(1));
		}
		return list;
	}

	/**
	 * 获取MD5字符串
	 * @param str
	 * @return
	 */
	public static String getMD5(String str) {
		String result =  MD5.getInstance().getHash(str);
		return result;
	}

	public static boolean isInteger(String str, Integer big, Integer small) {
		boolean returnb;
		if (isNotEmpty(str)) {
			try {
				int intStr = Integer.parseInt(str);
				if ((big.intValue() == 0) && (small.intValue() == 0)) {
					returnb = true;
				} else if ((intStr <= big.intValue()) && (intStr >= small.intValue())) {
					returnb = true;
				} else {
					returnb = false;
				}
			} catch (Exception e) {
				returnb = false;
			}
		} else {
			returnb = false;
		}

		return returnb;
	}

	/**
	 * 判断字符串是否是电话号码
	 * @param telephone
	 * @return
	 */
	public static boolean isPhone(String telephone) {
		boolean b;
		if (isEmpty(telephone)) {
			b = false;
		} else {
			Pattern p = Pattern.compile("^[1]([3|5|4|7|8][0-9]{1})[0-9]{8}$");
			Matcher m = p.matcher(telephone);
			b = m.matches();
		}
		return b;
	}
}
