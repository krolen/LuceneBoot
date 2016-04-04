package my.twister.bootlucene.service;

import my.twister.bootlucene.AppConfig;
import my.twister.bootlucene.utils.LogAware;
import my.twister.bootlucene.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;

/**
 * Created by kkulagin on 3/15/2016.
 */
@Service
public class ControlService implements LogAware, ApplicationListener<ContextStartedEvent> {

  @Autowired
  private LuceneService luceneService;
  @Autowired
  private AppConfig appConfig;

  @PostConstruct
  public void init() {
  }

  @Override
  public void onApplicationEvent(ContextStartedEvent event) {
    schedule();
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
    if (((intervalsNumber + 1) % appsNumber) == thisAppNumber) {
      log().info("Resetting lucene index for application {}", thisAppNumber);
      luceneService.reset();
    }
  }

}
