package my.twister.bootlucene.utils;

import my.twister.utils.Utils;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by kkulagin on 3/21/2016.
 */
public class UtilsTest {

  @Test
  public void testGetIntervalsNumberSinceDayStart() throws Exception {
    Instant now = Instant.now();
    int intervalsNumberSinceDayStart = Utils.getIntervalsNumberSinceDayStart(now, Duration.ofMinutes(30).toMillis());
    System.out.println(intervalsNumberSinceDayStart);
  }
}