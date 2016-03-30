package my.test.service;

import my.test.AppConfig;
import my.test.utils.LogAware;
import my.test.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
//    long appsInterval = appConfig.getAppsInterval();
//    int thisAppNumber = appConfig.getThisAppNumber();
//    int appsNumber = appConfig.getAppsNumber();
//
//    ScheduledExecutorService service = Executors.newScheduledThreadPool(1, new DefaultManagedAwareThreadFactory() {
//      @Override
//      public Thread newThread(Runnable r) {
//        Thread thread = super.newThread(r);
//        thread.setDaemon(true);
//        return thread;
//      }
//    });
//
//    service.schedule((Runnable) () -> {
//      process(Instant.now(), appsInterval, thisAppNumber, appsNumber);
//    }, appsInterval / appsNumber / 2, TimeUnit.MINUTES);
  }

//  @Scheduled(cron = "0 */5 * * * *")
  @Scheduled(cron = "0 15/30 * * * *")
  public void schedule() {
    log().info("Scheduling next control task execution");
    long appsInterval = appConfig.getAppsInterval();
    int thisAppNumber = appConfig.getThisAppNumber();
    int appsNumber = appConfig.getAppsNumber();
    process(Instant.now(), appsInterval, thisAppNumber, appsNumber);
  }

  void process(Instant now, long appsInterval, int thisAppNumber, int appsNumber) {
    int intervalsNumber = Utils.getIntervalsNumberSinceDayStart(now, appsInterval);
    if ((intervalsNumber % appsNumber) != thisAppNumber) {
      log().info("Resetting lucene index for application {}", thisAppNumber);
      luceneService.reset();
    }
  }

}
