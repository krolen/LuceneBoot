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
    int appNumber = appConfig.getAppNumber();
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
      Instant now = Instant.now();
      LocalDateTime dateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
      LocalDateTime dayStart = dateTime.with(LocalTime.MIN);
      long sinceDayStart = Duration.between(dayStart, dateTime).toMillis();
      int intervalsNumber = (int) (sinceDayStart / appsInterval);
      if((intervalsNumber % appsNumber) != appNumber) {
        log().info("Resetting lucene index for application {}", appNumber);
        luceneService.reset();
      }
    }, appsInterval/appsNumber, TimeUnit.MILLISECONDS);
  }
}
