package my.twister.bootlucene.service;

import lombok.SneakyThrows;
import my.twister.bootlucene.AppConfig;
import my.twister.utils.LogAware;
import my.twister.utils.Utils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.MMapDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kkulagin
 * @since 07.02.2016
 */
@Service
public class LuceneService implements LogAware {

  @Autowired
  private AppConfig appConfig;

  private ReferenceManager<IndexSearcher> searcherManager;
  private IndexWriter indexWriter;
  private AtomicInteger indexed = new AtomicInteger();
  private StandardAnalyzer analyzer;
  private ControlledRealTimeReopenThread<IndexSearcher> nrtReopenThread;
  private long from;
  private long to;
  private volatile Path path;

  @PostConstruct
  public void init() throws IOException, QueryNodeException {
    init(Instant.now().minusMillis(appConfig.getAppsInterval() * appConfig.getAppsNumber()));
  }

  public void index(long id, long time, String content) throws IOException {
    if (time < from || time > to) {
      log().error("Tweet {} with time {} is not in interval {}-{}. Skipping", id, time, from, to);
    } else {
      Document doc = new Document();
      doc.add(new LongField("id", id, Field.Store.YES));
      doc.add(new LongField("time", time, Field.Store.NO));
      doc.add(new TextField("content", content, Field.Store.NO));
      indexWriter.addDocument(doc);
      if (indexed.incrementAndGet() % 10000 == 0) {
        searcherManager.maybeRefresh();
      }
      if (indexed.get() % 100000 == 0) {
        indexWriter.commit();
      }
    }
  }

  public TopDocs search(Query query, Integer hitsCountToReturn) throws IOException {
    IndexSearcher searcher = null;
    try {
      searcher = searcherManager.acquire();
      TopDocs docs = searcher.search(query, Optional.ofNullable(hitsCountToReturn).orElse(appConfig.getLucene().getHitsToReturn()));
      log().debug("Found " + docs.totalHits + " docs for counter=1");
      return docs;
    } finally {
      Optional.ofNullable(searcher).ifPresent((reference) -> {
        try {
          searcherManager.release(reference);
        } catch (IOException ignored) {
        }
      });
    }
  }

  public Set<Long> searchBig(Query query, Integer hitsCountToReturn, String path) throws IOException {
    IndexSearcher searcher = null;
    try {
      searcher = searcherManager.acquire();
      TopDocs docs = searcher.search(query, Optional.ofNullable(hitsCountToReturn).orElse(appConfig.getLucene().getHitsToReturn()));
      long totalHits = docs.totalHits;
      ScoreDoc[] scoreDocs = docs.scoreDocs;
      for (ScoreDoc scoreDoc : scoreDocs) {
//        scoreDoc.
      }
      return null;
    } finally {
      Optional.ofNullable(searcher).ifPresent((reference) -> {
        try {
          searcherManager.release(reference);
        } catch (IOException ignored) {
        }
      });
    }
  }

  public Query parse(String query) throws QueryNodeException {
    return new StandardQueryParser(analyzer).parse(query, "content");
  }

  private Path createPath(long time) {
      LocalDateTime timeFrom = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
      return Paths.get(appConfig.getLucene().getIndexFilePath()).
          resolve(String.valueOf(appConfig.getThisAppNumber())).
          resolve(timeFrom.format(new DateTimeFormatterBuilder().appendPattern("YY-MM-dd-HH-mm").toFormatter()));
  }

  @SneakyThrows
  synchronized void reset() {
    log().info("Cleaning up resources, amount of docs to clear: " + indexed.get());
//    if (indexed.getAndSet(0) > 0) {
      try {
        indexWriter.commit();
        indexWriter.close();
        nrtReopenThread.close();
        searcherManager.close();
        System.gc();
      } catch (Exception e) {
        log().error("Error cleaning up resources", e);
      } finally {
        try {
          FileSystemUtils.deleteRecursively(path.toFile());
        } catch (Exception ignored) {
          log().warn("Error deleting directory", ignored);
        }
        indexed.set(0);
      }
      log().info("Re-initializing");
      init(Instant.now());
//    } else {
//      log().info("Nothing to clean");
//    }
  }

  private void init(Instant instant) throws QueryNodeException, IOException {
    int thisAppNumber = appConfig.getThisAppNumber();
    long appsInterval = appConfig.getAppsInterval();
    int appsNumber = appConfig.getAppsNumber();

    from = calculateCurrentIntervalStart(instant, appsNumber, thisAppNumber, appsInterval);
    to = from + appConfig.getDuration();
    log().info("Resetting service for app {} for dates: {} - {}", thisAppNumber, from, to);
    analyzer = new StandardAnalyzer();
    path = createPath(from);
    // TODO: 07.02.2016 offheap
    MMapDirectory index = new MMapDirectory(path);

    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    indexWriter = new IndexWriter(index, config);

    //=========================================================
    // This bit is specific to NRT
    //=========================================================
    TrackingIndexWriter trackingIndexWriter = new TrackingIndexWriter(indexWriter);
    searcherManager = new SearcherManager(indexWriter, true, null);
    //=========================================================
    // This thread handles the actual reader reopening.
    //=========================================================
    nrtReopenThread = new ControlledRealTimeReopenThread<>(trackingIndexWriter,
        searcherManager, appConfig.getLucene().getRefreshIndexMinInSeconds() + 10, appConfig.getLucene().getRefreshIndexMinInSeconds());
    nrtReopenThread.setName("NRT Reopen Thread");
    nrtReopenThread.setPriority(Math.min(Thread.currentThread().getPriority() + 2, Thread.MAX_PRIORITY));
    nrtReopenThread.setDaemon(true);
    nrtReopenThread.start();

    TopDocs topDocs = search(parse("*:*"), 1);
    log().info("Setting current indexed documents to " + topDocs.totalHits);
    indexed.set(topDocs.totalHits);
    log().info("Service started");
  }

  private static long calculateCurrentIntervalStart(Instant now, int appsNumber, int thisAppNumber, long appsInterval) {
    int intervalsNumberSinceDayStart = Utils.getIntervalsNumberSinceDayStart(now, appsInterval);
    int remainder = intervalsNumberSinceDayStart % appsNumber;
    long dayStart = Utils.toMillis(Utils.getDayStart(now));
    long start = dayStart + (intervalsNumberSinceDayStart - remainder) * appsInterval;
    start += appsInterval * (thisAppNumber == 0 ? appsNumber : thisAppNumber);
    return start;
  }

}
