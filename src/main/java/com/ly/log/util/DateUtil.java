package com.ly.log.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateUtil {
	/**
	 * Field value: Year
	 */
	public static final int YEAR = 1;

	/**
	 * Field value: Month
	 */
	public static final int MONTH = 2;

	/**
	 * Field value: Day
	 */
	public static final int DAY = 3;
	/**
	 * Field value: Week 周
	 */
	public static final int WEEK = 4;

	/**
	 * Field value: Hour
	 */
	public final static int HOUR = 10;

	/**
	 * Field value: Hour of Day
	 */
	public final static int HOUR_OF_DAY = 11;

	/**
	 * Field value: Minute
	 */
	public final static int MINUTE = 12;

	/**
	 * Field value: Second
	 */
	public final static int SECOND = 13;
	/**
	 * 获得本年第一天日期
	 */
	public static String formatDate(Date date, String format) {
		if (date == null)
			return "";
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}

	public static String getDateString(Date date) {
		return formatDate(date, "yyyy-MM-dd");
	}

	/**
	 *
	 * @param str
	 * @return
	 */
	public static String fixTimestamp(String str) {
		if (str.indexOf(':') == -1)
			return qualify(str) + " 00:00:00";
		else {
			int i = str.indexOf(' ');
			return qualify(str.substring(0, i)) + str.substring(i);
		}
	}

	private static String qualify(String dateStr) {
		if (dateStr.length() == 10)
			return dateStr;
		String[] sec = dateStr.split("-");
		if (sec.length == 3) {
			StringBuilder buf = new StringBuilder(10);
			buf.append(sec[0]);
			buf.append("-");
			if (sec[1].length() == 1)
				buf.append("0");
			buf.append(sec[1]);
			buf.append("-");
			if (sec[2].length() == 1)
				buf.append("0");
			buf.append(sec[2]);
			return buf.toString();
		} else
			return dateStr;
	}

	public static String fixTime(String str) {
		if (str.indexOf(':') == -1)
			return "00:00:00";
		int b = str.indexOf(' '), e = str.indexOf('.');
		if (b == -1)
			b = 0;
		if (e == -1)
			e = str.length();
		return str.substring(b, e);
	}

	public static String getHours(long milliSecs) {
		long h = milliSecs / 3600000, hm = milliSecs % 3600000;
		long m = hm / 60000, mm = hm % 60000;
		long s = mm / 1000, sm = mm % 1000;
		return StrUtil.concat(Long.toString(h), ":", Long.toString(m), ":",
				Long.toString(s), ".", Long.toString(sm));
	}

	public static int daysInMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public static int dayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	public static int yearOf(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	public static int dayOfYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_YEAR);
	}

	public static int dayOfWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	public static String toString(Date date) {
		if (date == null)
			return "";
		Timestamp t = new Timestamp(date.getTime());
		return t.toString();
	}

	public static Date incYear(Date date, int years) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, years);
		return cal.getTime();
	}

	public static Date incMonth(Date date, int months) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, months);
		return cal.getTime();
	}

	public static int hourOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public static Date incDay(Date date, long days) {
		return new Date(date.getTime() + 86400000 * days);
	}

	public static Date incSecond(Date date, long seconds) {
		return new Date(date.getTime() + 1000 * seconds);
	}

	public static Boolean isSameDay(Date date1,Date date2){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ");
	    String s1 = sdf.format(date1);
	    String s2 = sdf.format(date2);
	    if(s1.equals(s2))return true;
	    else return false;
	}

	/**
	 *
	 * @param d1
	 * @param formate
	 * @return
	 * String 转 date ""yyyy-MM-dd HH:mm:ss  对应截取
	 */
	public static Date stringToDate(String d1,String formate){
		Date d2 = null  ;
		try {
			 DateFormat df = new SimpleDateFormat (formate.trim());
		       d2 = df.parse(d1.trim());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d2;
	}

	/**
	 * Add value on special field of date
	 *
	 * @param iField
	 *            Field which need add value
	 * @param iValue
	 *            Value which will be added
	 * @param date
	 *            Basic date
	 * @return New date
	 */
	public static Date dateAdd(int iField, int iValue, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		switch (iField) {
		case DateUtil.YEAR:
			cal.add(Calendar.YEAR, iValue);
			break;
		case DateUtil.MONTH:
			cal.add(Calendar.MONTH, iValue);
			break;
		case DateUtil.DAY:
			cal.add(Calendar.DATE, iValue);
			break;
		case DateUtil.HOUR:
			cal.add(Calendar.HOUR, iValue);
			break;
		case DateUtil.HOUR_OF_DAY:
			cal.add(Calendar.HOUR_OF_DAY, iValue);
			break;
		case DateUtil.MINUTE:
			cal.add(Calendar.MINUTE, iValue);
			break;
		case DateUtil.SECOND:
			cal.add(Calendar.SECOND, iValue);
			break;
		case DateUtil.WEEK:
			cal.add(Calendar.DATE, iValue*7);
			break;
		default:
			break;
		}
		return cal.getTime();
	}

	public static int getIntervalDays(Date fDate, Date oDate) {
		if (null == fDate || null == oDate) {
			return -1;
		}
		long intervalMilli = (fDate.getTime() - oDate.getTime())/1000;
		return (int) (intervalMilli / (24 * 60 * 60));

	}
}
