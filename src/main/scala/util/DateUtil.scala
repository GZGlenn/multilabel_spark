package util

import java.text.{ParsePosition, SimpleDateFormat}
import java.util.{Calendar, Date, GregorianCalendar}

/**
  * Created by glennbinhu on 2017/8/17.
  */
object DateUtil {

  /**
    * 把日期字符串格式化成日期类型
    *
    * @param dateStr
    * @param format(yyyMMdd)
    * @return
    */
  def convert2Date(dateStr: String, format: String): Date = {
    val simple = new SimpleDateFormat(format)
    var result : Date = null
    try {
      simple.setLenient(true)
      result = simple.parse(dateStr, new ParsePosition(0))
    } catch {
      case e: Exception => println(e.getStackTrace)
    }
    result
  }


  /**
    * 把日期类型格式化成字符串
    *
    * @param date
    * @param format
    * @return
    */
  def convert2String(date: Date, format: String): String = {
    val formater = new SimpleDateFormat(format)
    try
      formater.format(date)
    catch {
      case e: Exception =>
        null
    }
  }

  /**
    * 获取当前日期
    *
    * @param format
    * @return
    */
  def getCurrentDate(format: String): String = new SimpleDateFormat(format).format(new Date)

  /**
    * 获取时间戳
    *
    * @return
    */
  def getTimestamp: Long = System.currentTimeMillis

  /**
    * 获取月份的天数
    *
    * @param year
    * @param month
    * @return
    */
  def getDaysOfMonth(year: Int, month: Int): Int = {
    val calendar = Calendar.getInstance
    calendar.set(year, month - 1, 1)
    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  }

  /**
    * 获取日期的年
    *
    * @param date
    * @return
    */
  def getYear(date: Date): Int = {
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.get(Calendar.YEAR)
  }

  /**
    * 获取日期的月
    *
    * @param date
    * @return
    */
  def getMonth(date: Date): Int = {
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.get(Calendar.MONTH) + 1
  }

  /**
    * 获取日期的日
    *
    * @param date
    * @return
    */
  def getDay(date: Date): Int = {
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.get(Calendar.DATE)
  }

  /**
    * 获取日期的时
    *
    * @param date
    * @return
    */
  def getHour(date: Date): Int = {
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.get(Calendar.HOUR)
  }

  /**
    * 获取日期的分种
    *
    * @param date
    * @return
    */
  def getMinute(date: Date): Int = {
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.get(Calendar.MINUTE)
  }

  /**
    * 获取日期的秒
    *
    * @param date
    * @return
    */
  def getSecond(date: Date): Int = {
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.get(Calendar.SECOND)
  }

  /**
    * 获取星期几
    *
    * @param date
    * @return
    */
  def getWeekDay(date: Date): Int = {
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    dayOfWeek - 1
  }

  /**
    * 获取哪一年共有多少周
    *
    * @param year
    * @return
    */
  def getMaxWeekNumOfYear(year: Int): Int = {
    val c = new GregorianCalendar
    c.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
    getWeekNumOfYear(c.getTime)
  }

  /**
    * 取得某天是一年中的多少周
    *
    * @param date
    * @return
    */
  def getWeekNumOfYear(date: Date): Int = {
    val c = new GregorianCalendar
    c.setFirstDayOfWeek(Calendar.MONDAY)
    c.setMinimalDaysInFirstWeek(7)
    c.setTime(date)
    c.get(Calendar.WEEK_OF_YEAR)
  }

  /**
    * 取得某天所在周的第一天
    *
    * @param date
    * @return
    */
  def getFirstDayOfWeek(date: Date): Date = {
    val c = new GregorianCalendar
    c.setFirstDayOfWeek(Calendar.MONDAY)
    c.setTime(date)
    c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek)
    c.getTime
  }

  /**
    * 取得某天所在周的最后一天
    *
    * @param date
    * @return
    */
  def getLastDayOfWeek(date: Date): Date = {
    val c = new GregorianCalendar
    c.setFirstDayOfWeek(Calendar.MONDAY)
    c.setTime(date)
    c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek + 6)
    c.getTime
  }

  /**
    * 取得某年某周的第一天 对于交叉:2008-12-29到2009-01-04属于2008年的最后一周,2009-01-05为2009年第一周的第一天
    *
    * @param year
    * @param week
    * @return
    */
  def getFirstDayOfWeek(year: Int, week: Int): Date = {
    val calFirst = Calendar.getInstance
    calFirst.set(year, 0, 7)
    var firstDate = getFirstDayOfWeek(calFirst.getTime)
    val firstDateCal = Calendar.getInstance
    firstDateCal.setTime(firstDate)
    val c = new GregorianCalendar
    c.set(Calendar.YEAR, year)
    c.set(Calendar.MONTH, Calendar.JANUARY)
    c.set(Calendar.DATE, firstDateCal.get(Calendar.DATE))
    val cal = c.clone.asInstanceOf[GregorianCalendar]
    cal.add(Calendar.DATE, (week - 1) * 7)
    firstDate = getFirstDayOfWeek(cal.getTime)
    firstDate
  }

  /**
    * 取得某年某周的最后一天 对于交叉:2008-12-29到2009-01-04属于2008年的最后一周, 2009-01-04为
    * 2008年最后一周的最后一天
    *
    * @param year
    * @param week
    * @return
    */
  def getLastDayOfWeek(year: Int, week: Int): Date = {
    val calLast = Calendar.getInstance
    calLast.set(year, 0, 7)
    val firstDate = getLastDayOfWeek(calLast.getTime)
    val firstDateCal = Calendar.getInstance
    firstDateCal.setTime(firstDate)
    val c = new GregorianCalendar
    c.set(Calendar.YEAR, year)
    c.set(Calendar.MONTH, Calendar.JANUARY)
    c.set(Calendar.DATE, firstDateCal.get(Calendar.DATE))
    val cal = c.clone.asInstanceOf[GregorianCalendar]
    cal.add(Calendar.DATE, (week - 1) * 7)
    val lastDate = getLastDayOfWeek(cal.getTime)
    lastDate
  }


  private def add(date: Date, calendarField: Int, amount: Int) = if (date == null) throw new IllegalArgumentException("The date must not be null")
  else {
    val c = Calendar.getInstance
    c.setTime(date)
    c.add(calendarField, amount)
    c.getTime
  }

  /*
   * 1则代表的是对年份操作， 2是对月份操作， 3是对星期操作， 5是对日期操作， 11是对小时操作， 12是对分钟操作， 13是对秒操作，
   * 14是对毫秒操作
   */

  /**
    * 增加年
    *
    * @param date
    * @param amount
    * @return
    */
  def addYears(date: Date, amount: Int): Date = add(date, 1, amount)

  /**
    * 增加月
    *
    * @param date
    * @param amount
    * @return
    */
  def addMonths(date: Date, amount: Int): Date = add(date, 2, amount)

  /**
    * 增加周
    *
    * @param date
    * @param amount
    * @return
    */
  def addWeeks(date: Date, amount: Int): Date = add(date, 3, amount)

  /**
    * 增加天
    *
    * @param date
    * @param amount
    * @return
    */
  def addDays(date: Date, amount: Int): Date = add(date, 5, amount)

  /**
    * 增加时
    *
    * @param date
    * @param amount
    * @return
    */
  def addHours(date: Date, amount: Int): Date = add(date, 11, amount)

  /**
    * 增加分
    *
    * @param date
    * @param amount
    * @return
    */
  def addMinutes(date: Date, amount: Int): Date = add(date, 12, amount)

  /**
    * 增加秒
    *
    * @param date
    * @param amount
    * @return
    */
  def addSeconds(date: Date, amount: Int): Date = add(date, 13, amount)

  /**
    * 增加毫秒
    *
    * @param date
    * @param amount
    * @return
    */
  def addMilliseconds(date: Date, amount: Int): Date = add(date, 14, amount)


  /**
    * time差
    *
    * @param before
    * @param after
    * @return
    */
  def diffTimes(before: Date, after: Date): Long = after.getTime - before.getTime

  /**
    * 秒差
    *
    * @param before
    * @param after
    * @return
    */
  def diffSecond(before: Date, after: Date): Long = (after.getTime - before.getTime) / 1000

  /**
    * 分种差
    *
    * @param before
    * @param after
    * @return
    */
  def diffMinute(before: Date, after: Date): Int = (after.getTime - before.getTime).toInt / 1000 / 60

  /**
    * 时差
    *
    * @param before
    * @param after
    * @return
    */
  def diffHour(before: Date, after: Date): Int = (after.getTime - before.getTime).toInt / 1000 / 60 / 60

  /**
    * 天数差
    *
    * @param before
    * @param after
    * @return
    */
  def diffDay(before: Date, after: Date): Int = String.valueOf((after.getTime - before.getTime) / 86400000).toInt

  /**
    * 月差
    *
    * @param before
    * @param after
    * @return
    */
  def diffMonth(before: Date, after: Date): Int = {
    var monthAll = 0
    val yearsX = diffYear(before, after)
    val c1 = Calendar.getInstance
    val c2 = Calendar.getInstance
    c1.setTime(before)
    c2.setTime(after)
    val monthsX = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH)
    monthAll = yearsX * 12 + monthsX
    val daysX = c2.get(Calendar.DATE) - c1.get(Calendar.DATE)
    if (daysX > 0) monthAll = monthAll + 1
    monthAll
  }

  /**
    * 年差
    *
    * @param before
    * @param after
    * @return
    */
  def diffYear(before: Date, after: Date): Int = getYear(after) - getYear(before)

  /**
    * 设置23:59:59
    *
    * @param date
    * @return
    */
  def setEndDay(date: Date): Date = {
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.getTime
  }

  /**
    * 设置00:00:00
    *
    * @param date
    * @return
    */
  def setStartDay(date: Date): Date = {
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.getTime
  }

  def test(): Unit = {

  }

}
