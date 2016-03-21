package my.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kkulagin on 3/15/2016.
 */
public interface LogAware {

  default Logger log() {
    return LoggerFactory.getLogger(getClass());
  }

}
