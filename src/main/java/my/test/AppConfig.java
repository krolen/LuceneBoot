package my.test;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author kkulagin
 * @since 07.02.2016
 */
@ConfigurationProperties("app")
@Component
@Getter
public class AppConfig {

  @Value(value = "#{lucene.refreshIndexMin}")
  private int refreshIndexMin;
  @Value(value = "#{lucene.hitsToReturn}")
  private int hitsToReturn;
  @Value(value = "#{lucene.indexFilePath}")
  private String indexFilePath;
//  public int getRefreshIndexMin() {
//    return refreshIndexMin;
//  }
//
//  public String getIndexFilePath() {
//    return indexFilePath;
//  }
}
