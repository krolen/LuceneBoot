package my.test.service;

import lombok.SneakyThrows;
import my.test.AppConfig;
import my.test.LogAware;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.MMapDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
  private AtomicLong indexed = new AtomicLong();
  private StandardAnalyzer analyzer;

  @PostConstruct
  public void init() throws IOException {
    analyzer = new StandardAnalyzer();
    // TODO: 07.02.2016 offheap
    MMapDirectory index = new MMapDirectory(getPath());

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
    ControlledRealTimeReopenThread<IndexSearcher> nrtReopenThread = new ControlledRealTimeReopenThread<>(trackingIndexWriter,
        searcherManager, appConfig.getLucene().getRefreshIndexMinInSeconds() + 10, appConfig.getLucene().getRefreshIndexMinInSeconds());
    nrtReopenThread.setName("NRT Reopen Thread");
    nrtReopenThread.setPriority(Math.min(Thread.currentThread().getPriority() + 2, Thread.MAX_PRIORITY));
    nrtReopenThread.setDaemon(true);
    nrtReopenThread.start();
  }

  public void index(long id, long time, String content) throws IOException {
    Document doc = new Document();
    doc.add(new LongField("id", id, Field.Store.YES));
    doc.add(new LongField("time", time, Field.Store.NO));
    doc.add(new StringField("content", content, Field.Store.NO));
    indexWriter.addDocument(doc);
    if (indexed.incrementAndGet() % 10000 == 0) {
      searcherManager.maybeRefresh();
    }
    if (indexed.incrementAndGet() % 100000 == 0) {
      indexWriter.commit();
    }
  }

  public TopDocs search(Query query, Integer hitsCountToReturn) throws IOException {
    IndexSearcher searcher = null;
    try {
      searcher = searcherManager.acquire();
      TopDocs docs = searcher.search(query, Optional.ofNullable(hitsCountToReturn).orElse(appConfig.getLucene().getHitsToReturn()));
      System.out.println("Found " + docs.totalHits + " docs for counter=1");
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

  private Path getPath() {
    return Paths.get(appConfig.getLucene().getIndexFilePath()).resolve(String.valueOf(appConfig.getAppNumber()));
  }

  @SneakyThrows
  synchronized void reset() {
    if(indexed.getAndSet(0) > 0) {
      log().info("Cleaning up resources");
      try {
        indexWriter.commit();
        searcherManager.close();
        Files.delete(getPath());
        System.gc();
      } catch (Exception e) {
        log().warn("Error cleaning up resources", e);
      }
      log().info("Re-initializing");
      init();
    } else {
      log().info("Nothing to clean");
    }
  }
}
