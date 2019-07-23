package com.taurus.core.util;

import java.util.Calendar;

public class DateUtils {
	
	// 获取当天的开始时间
	private static void setDayBegin(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}

	// 获取当天的结束时间
	private static void setDayEnd(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
	}

	private static int getTimeSec(Calendar cal) {
		int timeSec = (int) (cal.getTimeInMillis() / 1000);
		return timeSec;
	}
	
	/**
	 * 今天开始时间
	 * @return
	 */
	public static int getBeginDay() {
		Calendar cal= Calendar.getInstance();
		setDayBegin(cal);
		return getTimeSec(cal);
	}
	
	/**
	 * 今天结束时间
	 * @return
	 */
	public static int getEndDay() {
		Calendar cal= Calendar.getInstance();
		setDayEnd(cal);
		return getTimeSec(cal);
	}
	
	
	/**
	 *  获取昨天的开始时间
	 * @return
	 */
	public static int getBeginLastday() {
		return getBeginDay() - 24*60*60;
	}

	/**
	 *  获取昨天的结束时间
	 * @return
	 */
	public static int getEndLastDay() {
		return getBeginDay()-1;
	}


	/**
	 * 获取本周的开始时间
	 * @return
	 */
	public static int getBeginWeek() {
		Calendar cal = Calendar.getInstance();
		int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayofweek == 1) {
			dayofweek += 7;
		}
		cal.add(Calendar.DATE, 2 - dayofweek);
		setDayBegin(cal);
		return getTimeSec(cal);
	}

	/**
	 * 获取本周的结束时间
	 * @return
	 */
	public static int getEndWeek() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getBeginWeek()*1000L);
		cal.add(Calendar.DAY_OF_WEEK, 6);
		setDayEnd(cal);
		return getTimeSec(cal);
	}

	/**
	 * 获取上周的开始时间
	 * @return
	 */
	public static int getBeginLastWeek() {
		Calendar cal = Calendar.getInstance();
		int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayofweek == 1) {
			dayofweek += 7;
		}
		cal.add(Calendar.DATE, 2 - dayofweek - 7);
		setDayBegin(cal);
		return getTimeSec(cal);
	}

	/**
	 * 获取上周的结束时间
	 * @return
	 */
	public static int getEndLastWeek() {
		return getBeginWeek() -1 ;
	}

	/**
	 *  获取本月的开始时间
	 */
	public static int getBeginMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 0);  
        cal.set(Calendar.DAY_OF_MONTH, 1);  
        setDayBegin(cal);
		return getTimeSec(cal);
	}

	/**
	 *  获取本月的结束时间
	 * @return
	 */
	public static int getEndMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);  
        cal.set(Calendar.DAY_OF_MONTH, 0); 
        setDayEnd(cal);
        return getTimeSec(cal);
	}

	/**
	 *  获取上月的开始时间
	 */
	public static int getBeginLastMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);  
        cal.set(Calendar.DAY_OF_MONTH, 1); 
        setDayBegin(cal);
        return getTimeSec(cal);
	}

	/**
	 * 获取上月的结束时间
	 * @return
	 */
	public static int getEndLastMonth() {
        return getBeginMonth() -1;
	}

	/**
	 * 两个日期相减得到的天数
	 * @param begin
	 * @param end
	 * @return
	 */
	public static int getDiffDays(int begin, int end) {
		int diff = (end- begin) / (60 * 60 * 24);
		return diff;
	}

	/**
	 *  两个日期相减得到的毫秒数
	 * @param begin
	 * @param end
	 * @return
	 */
	public static int dateDiff(int begin, int end) {
		return end - begin;
	}



}
