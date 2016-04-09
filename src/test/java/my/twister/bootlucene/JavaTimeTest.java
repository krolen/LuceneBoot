package my.twister.bootlucene;

import org.junit.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;

/**
 * Created by kkulagin on 3/15/2016.
 */
public class JavaTimeTest {

  @Test
  public void testStartOfTheDay() {
    Instant now = Instant.now();
    LocalDateTime dateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
    LocalDateTime dayStart = dateTime.with(LocalTime.MIN);
    Duration duration = Duration.between(dayStart, dateTime);
    long hours = duration.toHours();
    System.out.println(hours);
  }

  @Test
  public void testTimeFormat() {
    LocalDateTime now = LocalDateTime.now();
    System.out.println(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    System.out.println(now.format(
        new DateTimeFormatterBuilder().appendPattern("YY-MM-dd-HH-mm").toFormatter()));
  }

  @Test
  public void testHourStart() {
    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
    LocalDateTime nextHourStart = dateTime.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
    int hour = nextHourStart.getHour();
    System.out.println(hour);
  }

  @Test
  public void testTimeFormat2() {
    LocalDateTime localDateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    System.out.println(localDateTime.format(
        new DateTimeFormatterBuilder().appendPattern("YY-MM-dd-HH-mm").toFormatter()));
  }
}
