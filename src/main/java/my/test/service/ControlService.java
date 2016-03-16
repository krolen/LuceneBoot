package my.test.service;

import my.test.AppConfig;
import my.test.LogAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by kkulagin on 3/15/2016.
 */
@Service
public class ControlService implements LogAware {

  @Autowired
  private LuceneService luceneService;
  @Autowired
  private AppConfig appConfig;

  @PostConstruct
  public void init() {
    long appsInterval = appConfig.getAppsInterval();
    int thisAppNumber = appConfig.getThisAppNumber();
    int appsNumber = appConfig.getAppsNumber();

    ScheduledExecutorService service = Executors.newScheduledThreadPool(1, new DefaultManagedAwareThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread thread = super.newThread(r);
        thread.setDaemon(true);
        return thread;
      }
    });
    service.schedule((Runnable) () -> {
      process(Instant.now(), appsInterval, thisAppNumber, appsNumber);
    }, appsInterval / appsNumber / 2, TimeUnit.MINUTES);
  }

  void process(Instant now, long appsInterval, int thisAppNumber, int appsNumber) {
    LocalDateTime dateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
    LocalDateTime dayStart = dateTime.with(LocalTime.MIN);
    long sinceDayStart = Duration.between(dayStart, dateTime).toMillis();
    int intervalsNumber = (int) (sinceDayStart / appsInterval);
    if ((intervalsNumber % appsNumber) != thisAppNumber) {
      log().info("Resetting lucene index for application {}", thisAppNumber);
      luceneService.reset();
    }
  }
}
