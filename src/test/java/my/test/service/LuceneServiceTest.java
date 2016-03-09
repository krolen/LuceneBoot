package my.test.service;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
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
    luceneService.index(1L, "test number 1");
    luceneService.index(2L, "test number 2");
    luceneService.index(3L, "test number 3");
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testParse() throws Exception {
    Query q1 = luceneService.parse("content: sdf");
    Query q2 = luceneService.parse("content: sdf AND data: 55");
    Query q3 = luceneService.parse("content: sdf AND id: [100 TO *]");
    System.out.println("yo");
  }

  @Test
  public void testSearch() throws Exception {
    Query query = luceneService.parse("content: 'number 1'");
    TopDocs docs = luceneService.search(query, 1);
    assertThat
  }
}