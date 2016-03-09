package my.test;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author kkulagin
 * @since 07.02.2016
 */
@ConfigurationProperties("app")
@Component
@Getter
@Setter
public class AppConfig {

  private Lucene lucene;

  @Getter
  @Setter
  public static class Lucene {
    private int refreshIndexMinInSeconds;
    private int hitsToReturn;
    private String indexFilePath;
  }
}
