package my.test.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.*;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by kkulagin on 3/16/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ControlServiceTest {

  @Mock
  private LuceneService luceneService;
  @InjectMocks
  private ControlService controlService;

  @Test
  public void testProcess() throws Exception {
    Instant now = Instant.now();
    LocalDateTime dateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
    LocalDateTime dayStart = dateTime.with(LocalTime.MIN);
    LocalDateTime time = dayStart.plusMinutes(20);
    controlService.process(time.atZone(ZoneId.systemDefault()).toInstant(), Duration.ofMinutes(15).toMillis(), 2, 3);
    verify(luceneService, times(1)).reset();

    reset(luceneService);
    time = dayStart.plusMinutes(40);
    controlService.process(time.atZone(ZoneId.systemDefault()).toInstant(), Duration.ofMinutes(15).toMillis(), 2, 3);
    verify(luceneService, times(0)).reset();
  }
}