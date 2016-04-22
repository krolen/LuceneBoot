package my.twister.bootlucene.service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.sun.media.sound.ModelStandardDirector;
import lombok.SneakyThrows;
import my.twister.bootlucene.AppConfig;
import my.twister.bootlucene.map.LowercaseWhitespaceSacIndexAnalyzer;
import my.twister.bootlucene.map.LowercaseWhitespaceSacQueryAnalyzer;
import my.twister.chronicle.ChronicleQueueDataService;
import my.twister.utils.LogAware;
import my.twister.utils.Utils;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.VanillaBytes;
import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryParserHelper;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.MMapDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * @author kkulagin
 * @since 07.02.2016
 */
@Service
public class LuceneService implements LogAware {

  public static final int MAX_BIG_DOCS = 5_000_000;

  @Autowired
  private AppConfig appConfig;

  private ReferenceManager<IndexSearcher> searcherManager;
  private IndexWriter indexWriter;
  private AtomicInteger indexed = new AtomicInteger();
  private ControlledRealTimeReopenThread<IndexSearcher> nrtReopenThread;
  private long from;
  private long to;
  private volatile Path path;
  private ChronicleQueueDataService queueDataService = ChronicleQueueDataService.getInstance();

  @PostConstruct
  public void init() throws IOException, QueryNodeException {
    init(Instant.now());
  }

  public void index(byte[] id, long time, String content) throws IOException {
    Document doc = new Document();
    doc.add(new StoredField("id", id));
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

  public int searchInternalNoTime(Query query, Long from, Long to, int hitsCountToReturn, LongFunction<Void> f) throws IOException {
    final Set<String> fieldsToReturn = ImmutableSet.of("id");
    IndexSearcher searcher = null;
    try {
      Query queryRange = NumericRangeQuery.newLongRange("time", from, to, true, true);
      BooleanQuery booleanQuery = new BooleanQuery.Builder()
          .add(query, BooleanClause.Occur.MUST)
          .add(queryRange, BooleanClause.Occur.MUST)
          .build();
      searcher = searcherManager.acquire();
      Stopwatch stopwatch = Stopwatch.createStarted();
      TopDocs docs = searcher.search(booleanQuery, hitsCountToReturn);
      ScoreDoc[] scoreDocs = docs.scoreDocs;
      log().info("Search took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " found " + scoreDocs.length + " documents");
      stopwatch.reset().start();

      final IndexSearcher finalSearcher = searcher;
      Arrays.stream(scoreDocs).parallel().forEach(sd -> {
        try {
          Document doc = finalSearcher.doc(sd.doc, fieldsToReturn);
          Long tweetId = (Long) doc.getField("id").numericValue();
          f.apply(tweetId);
        } catch (IOException e) {
          log().error("Error reading doc from lucine", e);
        }
      });
      log().info("Writing took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
      return scoreDocs.length;
    } finally {
      Optional.ofNullable(searcher).ifPresent((reference) -> {
        try {
          searcherManager.release(reference);
        } catch (IOException ignored) {
        }
      });
    }
  }

  public int searchInternalNoTimeBytes(Query query, Long from, Long to, int hitsCountToReturn, Function<byte[], Void> f) throws IOException {
    final Set<String> fieldsToReturn = ImmutableSet.of("id");
    IndexSearcher searcher = null;
    try {
      Query queryRange = NumericRangeQuery.newLongRange("time", from, to, true, true);
      BooleanQuery booleanQuery = new BooleanQuery.Builder()
          .add(query, BooleanClause.Occur.MUST)
          .add(queryRange, BooleanClause.Occur.MUST)
          .build();
      searcher = searcherManager.acquire();
      Stopwatch stopwatch = Stopwatch.createStarted();
      TopDocs docs = searcher.search(booleanQuery, hitsCountToReturn);
      ScoreDoc[] scoreDocs = docs.scoreDocs;
      log().info("Search took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " found " + scoreDocs.length + " documents");

      stopwatch.reset().start();

//      long[] res = new long[scoreDocs.length];
      AtomicInteger i = new AtomicInteger(-1);
      final IndexSearcher finalSearcher = searcher;
      Arrays.stream(scoreDocs).parallel().forEach(sd -> {
        try {
          Document doc = finalSearcher.doc(sd.doc, fieldsToReturn);
          f.apply(doc.getField("id").binaryValue().bytes);
        } catch (IOException e) {
          log().error("cannot get doc", e);
        }
//        res[i.incrementAndGet()] = Longs.fromByteArray(doc.getField("id").binaryValue().bytes);
      });
//      for (ScoreDoc sd : scoreDocs) {
//        Document doc = searcher.doc(sd.doc, fieldsToReturn);
//        res[i.incrementAndGet()] = Longs.fromByteArray(doc.getField("id").binaryValue().bytes);
////        f.apply(doc.getField("id").binaryValue().bytes);
//      }
//      log().info("Reading ids took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));

//      stopwatch.reset().start();
//      for (long re : res) {
//        f.apply(Longs.toByteArray(re));
//      }
      log().info("Writing ids took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));

      return scoreDocs.length;
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
    return (Query) parser().parse(query, "content");
  }

  public IndexWriter getIndexWriter() {
    return indexWriter;
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

  private void init(Instant instant) throws IOException, QueryNodeException {
    int thisAppNumber = appConfig.getThisAppNumber();
    long appsInterval = appConfig.getAppsInterval();
    int appsNumber = appConfig.getAppsNumber();

    from = calculateCurrentIntervalStart(instant, appsNumber, thisAppNumber, appsInterval);
    to = from + appConfig.getDuration();
    log().info("Resetting service for app {} for dates: {} - {}", thisAppNumber, from, to);
    path = createPath(from);
    // TODO: 07.02.2016 offheap
    MMapDirectory index = new MMapDirectory(path);


    IndexWriterConfig config = new IndexWriterConfig(analyzer());
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

  private static long calculateCurrentIntervalStart(Instant instant, int appsNumber, int thisAppNumber, long appsInterval) {
    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    LocalDateTime nextHourStart = dateTime.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
    int closestUpperHour = nextHourStart.getHour();
    if (closestUpperHour % appsNumber == thisAppNumber) {
      return Utils.toMillis(nextHourStart);
    } else if ((closestUpperHour - 1) % appsNumber == thisAppNumber) {
      return Utils.toMillis(nextHourStart.minusHours(1));
    } else {
      return Utils.toMillis(nextHourStart.minusHours(2));
    }
  }


  protected Analyzer analyzer() {
    return new LowercaseWhitespaceSacIndexAnalyzer();
  }

  protected QueryParserHelper parser() {
    return new StandardQueryParser(queryAnalyzer());
  }

  protected Analyzer queryAnalyzer() {
    return new LowercaseWhitespaceSacQueryAnalyzer();
  }

}
