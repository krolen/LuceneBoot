package my.test.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 * Created by kkulagin on 3/21/2016.
 */
public class Utils {

  public static int getIntervalsNumberSinceDayStart(Instant instant, long intervalDuration) {
    return getIntervalsNumberSinceDayStart(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()), intervalDuration);
  }

  public static int getIntervalsNumberSinceDayStart(LocalDateTime dateTime, long intervalDuration) {
    LocalDateTime dayStart = dateTime.with(LocalTime.MIN);
    long sinceDayStart = Duration.between(dayStart, dateTime).toMillis();
    return (int) (sinceDayStart / intervalDuration);
  }

  public static LocalDateTime getDayStart(Instant instant) {
    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    LocalDateTime dayStart = dateTime.with(LocalTime.MIN);
    return dayStart;
  }

  public static long toMillis(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

  public static LocalDateTime toHourMinute(Instant instant, int hourMinute) {
    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    if (dateTime.getMinute() < hourMinute) {
      dateTime = dateTime.minus(1, ChronoUnit.HOURS);
    }
    dateTime = dateTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(hourMinute);
    return dateTime;
  }

}
