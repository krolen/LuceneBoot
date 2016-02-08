package my.test.service;

import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author kkulagin
 * @since 07.02.2016
 */

public class LuceneServiceTest {
  private LuceneService luceneService;

  public LuceneServiceTest(LuceneService luceneService) {
    this.luceneService = luceneService;
  }


  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testParse() throws Exception {
    Query q1 = luceneService.parse("content: sdf");
    Query q2 = luceneService.parse("content: sdf AND data: 55");
    System.out.println("yo");
  }
}