package my.test;

import org.junit.Test;

import java.time.*;
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
}
