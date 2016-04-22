package my.twister.bootlucene.service;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.Uninterruptibles;
import my.twister.utils.Utils;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

/**
 * @author kkulagin
 * @since 07.02.2016
 */

public class LuceneServiceTest {
  private LuceneService luceneService;

  public LuceneServiceTest(LuceneService luceneService) {
    this.luceneService = luceneService;
  }

  public void index() throws Exception {
    luceneService.index(Longs.toByteArray(1L), 100L, "test number 1");
    luceneService.index(Longs.toByteArray(2L), 100L, "test number 2");
    luceneService.index(Longs.toByteArray(3L), 100L, "test number 3");
  }

  @After
  public void tearDown() throws Exception {

  }

  public void testParse() throws Exception {
    Query q1 = luceneService.parse("content: sdf");
    Query q2 = luceneService.parse("content: sdf AND data: 55");
    Query q3 = luceneService.parse("content: sdf AND id: [100 TO *]");
    System.out.println("yo");
  }

  public void testSearch() throws Exception {
    Query query = luceneService.parse("content: 'number 1'");
    TopDocs docs = luceneService.search(query, 1);
  }


  public void testSearchBigNoTime() throws Exception {
    LongStream.range(0, 1_000_000).forEach((l) -> {
      try {
        luceneService.index(Longs.toByteArray(l), System.currentTimeMillis() - 5000, "test number " + l);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    luceneService.getIndexWriter().commit();
    System.out.println("Documents written");
    System.out.println("Documents written");
    System.out.println("Documents written");
    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

    Query query = luceneService.parse("*:*");
    Stopwatch started = Stopwatch.createStarted();
    long found = luceneService.searchInternalNoTimeBytes(query, System.currentTimeMillis() - Utils.MILLIS_PER_HOUR,
        System.currentTimeMillis(), 1_000_000, (l) -> {
          long value = Longs.fromByteArray(l);
          if(value % 1000 == 0) {
            System.out.println("value = " + value);
          }
          return null;
        });
    System.out.println("search done in " + started.elapsed(TimeUnit.MILLISECONDS));
    System.out.println("Documents found: " + found);
  }
}