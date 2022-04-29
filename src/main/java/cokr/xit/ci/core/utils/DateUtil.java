
package cokr.xit.ci.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Slf4j
public class DateUtil {







	/**
	 * <pre>메소드 설명: 절대시간(단위: second)을 현재 시간으로 반환 한다.</pre>
	 * @param sec
	 * @param fmt
	 * @return String 요청처리 후 응답객체
	 * @author: 박민규
	 * @date: 2021. 11. 19.
	 */
	public static String absTimeSecToDate(int sec, String fmt) {
		return absTimeSecToDate(sec * 1L, fmt);
	}
	/**
	 * <pre>메소드 설명: 절대시간(단위: second)을 현재 시간으로 반환 한다.</pre>
	 * @param sec
	 * @param fmt
	 * @return String 요청처리 후 응답객체
	 * @author: 박민규
	 * @date: 2021. 11. 19.
	 */
	public static String absTimeSecToDate(Long sec, String fmt) {
		if(sec==null)
			return null;
		return absTimeToDate(sec * 1000L, fmt);
	}
	/**
	 * <pre>메소드 설명: 절대시간(단위: ms)을 현재 시간으로 반환 한다.</pre>
	 * @param millionSec
	 * @param fmt
	 * @return String 요청처리 후 응답객체
	 * @author: 박민규
	 * @date: 2021. 11. 19.
	 */
	public static String absTimeToDate(Long millionSec, String fmt) {
		if(fmt == null || "".equals(fmt)) fmt = "yyyy-MM-dd HH:mm:ss";

		Date date = new Date(millionSec);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fmt);

		return simpleDateFormat.format(date);
	}


	/**
	 * <pre>메소드 설명: 시간을 절대시간(단위: sec)로 반환 한다.</pre>
	 * @param expireDt	만료일시(yyyyMMddHHmmss)
	 * @return Long 요청처리 후 응답객체
	 * @author: 박민규
	 * @date: 2021. 11. 19.
	 */
	public static Long dateToAbsTimeSec(String expireDt) {
		return dateToAbsTime(expireDt)/1000;
	}
	/**
	 * <pre>메소드 설명: 시간을 절대시간(단위: sec)로 반환 한다.</pre>
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param sec
	 * @return Long 요청처리 후 응답객체
	 * @author: 박민규
	 * @date: 2021. 11. 19.
	 */
	public static Long dateToAbsTimeSec(int year, int month, int day, int hour, int minute, int sec) {
		return dateToAbsTime(year, month, day, hour, minute, sec)/1000;
	}
	/**
	 * <pre>메소드 설명: 시간을 절대시간(단위: ms)로 반환 한다.</pre>
	 * @param expireDt	만료일시(yyyyMMddHHmmss)
	 * @return Long 요청처리 후 응답객체
	 * @author: 박민규
	 * @date: 2021. 11. 19.
	 */
	@SuppressWarnings("finally")
	public static Long dateToAbsTime(String expireDt) {
		if(expireDt==null)
			return 0L;

		expireDt = expireDt.replaceAll("[^0-9]", "");
		if("".equals(expireDt))
			return 0L;

		int year, month, day, hour, minute, sec;
		year = month = day = hour = minute = sec = 0;
		try {
			year   = Integer.parseInt(expireDt.substring(0,4));
			month  = Integer.parseInt(expireDt.substring(4,6));
			day    = Integer.parseInt(expireDt.substring(6,8));
			hour   = Integer.parseInt(expireDt.substring(8,10));
			minute = Integer.parseInt(expireDt.substring(10,12));
			sec    = Integer.parseInt(expireDt.substring(12,14));
		} catch(Exception e) {
			log.error(String.format("dateToAbsTime fail... expireDt is [%s]", expireDt));
		} finally {
			return dateToAbsTime(year, month, day, hour, minute, sec);
		}

	}
	/**
	 * <pre>메소드 설명: 시간을 절대시간(단위: ms)로 반환 한다.</pre>
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param sec
	 * @return Long 요청처리 후 응답객체
	 * @author: 박민규
	 * @date: 2021. 11. 19.
	 */
	@SuppressWarnings("deprecation")
	public static Long dateToAbsTime(int year, int month, int day, int hour, int minute, int sec) {
		if(month < 0)           month = 1;
		if(month > 11)          month = 12;
		if(day < 1 || day > 31) day = 1;
		if(hour < 0 || hour > 23)     hour = 0;
		if(minute < 0 || minute > 59) minute = 0;
		if(sec < 0 || sec > 59)       sec = 0;

		Date curDate = new Date (year-1900, month-1, day, hour, minute, sec);
		return curDate.getTime();
	}

	/**
	 * <pre>메소드 설명: 일수를 상대시간(단위: ms)로 반환 한다.</pre>
	 * @param days
	 * @return Long 요청처리 후 응답객체
	 * @author: 박민규
	 * @date: 2021. 11. 19.
	 */
	public static Long daysToRelTime(int days) {
		if(days<0) days=0;
		return days * 24L * 60L * 60L * 1000L;
	}

	/**
	 * <pre>메소드 설명: 일시(yyyyMMddHHmmss)를 LocalDateTime 으로 반환 한다.</pre>
	 * @param yyyyMMddHHmmss
	 * @return Long 요청처리 후 응답객체
	 * @author: 박민규
	 * @date: 2022. 3. 15.
	 */
	public static LocalDateTime toLocalDateTime(String yyyyMMddHHmmss){
		String date = Optional.ofNullable(yyyyMMddHHmmss).orElseGet(()->null);

		if(date==null) return null;

		date = date.replaceAll("[^0-9]", "");
		if(date.length()!=14)
			return null;


		return LocalDateTime.of(
				Integer.parseInt(date.substring(0,4))
				, Integer.parseInt(date.substring(4,6))
				, Integer.parseInt(date.substring(6,8))
				, Integer.parseInt(date.substring(8,10))
				, Integer.parseInt(date.substring(10,12))
				, Integer.parseInt(date.substring(12,14))
		);
	}

}
