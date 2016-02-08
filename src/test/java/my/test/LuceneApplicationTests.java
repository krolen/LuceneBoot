package my.test;

import my.test.service.LuceneService;
import my.test.service.LuceneServiceTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LuceneApplication.class)
public class LuceneApplicationTests {
  @Autowired
  private LuceneService luceneService;

  @Test
  public void contextLoads() throws Exception {
    new LuceneServiceTest(luceneService).testParse();
  }

}
