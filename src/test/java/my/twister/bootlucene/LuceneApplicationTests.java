package my.twister.bootlucene;

import my.twister.bootlucene.service.LuceneService;
import my.twister.bootlucene.service.LuceneServiceTest;
import org.junit.Before;
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
  private LuceneServiceTest luceneServiceTest;

  @Before
  public void setUp() throws Exception {
    luceneServiceTest = new LuceneServiceTest(luceneService);
  }

  @Test
  public void parse() throws Exception {
    luceneServiceTest.testParse();
  }

  @Test
  public void index() throws Exception {
    luceneServiceTest.index();
  }

  @Test
  public void search() throws Exception {
    luceneServiceTest.testSearch();
  }

  @Test
  public void searchBig() throws Exception {
    luceneServiceTest.testSearchBig();
  }

}
