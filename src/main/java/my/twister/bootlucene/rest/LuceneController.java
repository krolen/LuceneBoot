package my.twister.bootlucene.rest;

import com.google.common.primitives.Longs;
import my.twister.bootlucene.service.LuceneService;
import my.twister.utils.LogAware;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.validation.ValidationException;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author kkulagin
 * @since 07.02.2016
 */
@RestController
public class LuceneController implements LogAware {

  @Autowired
  private LuceneService luceneService;

  @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public TopDocs search(@RequestParam(value = "q") String query, @RequestParam(value = "max", required = false) Integer count)
      throws IOException, QueryNodeException {
    Query q = luceneService.parse(query);
    return luceneService.search(q, count);
  }

  @RequestMapping(value = "/searchBigNoTimeStream", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public StreamingResponseBody searchBigNoTime(@RequestParam(value = "q") String query,
                                               @RequestParam(value = "from") Long from,
                                               @RequestParam(value = "to") Long to,
                                               @RequestParam(value = "max", required = false) Integer count) throws IOException, QueryNodeException {
    if (to > System.currentTimeMillis() + 5000) {
      throw new ValidationException("'to' should be in the past");
    }

    Query q = luceneService.parse(query);
    final Integer finalCount = Optional.ofNullable(count).map(i -> Math.min(i, LuceneService.MAX_BIG_DOCS)).orElse(LuceneService.MAX_BIG_DOCS);
    ReentrantLock lock = new ReentrantLock();
    return outputStream -> {
      luceneService.searchInternalNoTime(q, from, to, finalCount, tweetId -> {
        try {
          lock.lock();
          outputStream.write(Longs.toByteArray(tweetId));
        } catch (IOException e) {
          log().error("Error streaming data", e);
        } finally {
          lock.unlock();
        }
        return null;
      });
      outputStream.flush();
    };
  }

  @RequestMapping(value = "/searchBigNoTimeStreamBytes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public StreamingResponseBody searchBigNoTimeBytes(@RequestParam(value = "q") String query,
                                                    @RequestParam(value = "from") Long from,
                                                    @RequestParam(value = "to") Long to,
                                                    @RequestParam(value = "max", required = false) Integer count) throws IOException, QueryNodeException {
    if (to > System.currentTimeMillis() + 5000) {
      throw new ValidationException("'to' should be in the past");
    }

    Query q = luceneService.parse(query);
    final Integer finalCount = Optional.ofNullable(count).map(i -> Math.min(i, LuceneService.MAX_BIG_DOCS)).orElse(LuceneService.MAX_BIG_DOCS);
    return outputStream -> {
      luceneService.searchInternalNoTimeBytes(q, from, to, finalCount, tweetId -> {
        try {
          outputStream.write(tweetId);
        } catch (IOException e) {
          log().error("Error streaming data", e);
        }
        return null;
      });
      outputStream.flush();
    };
  }


  @RequestMapping(value = "/put/{tweetId}/{time}", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void put(@PathVariable("tweetId") long tweetId, @PathVariable("time") Integer time, @RequestBody String body) {
    try {
      luceneService.index(Longs.toByteArray(tweetId), time, body);
    } catch (IOException e) {
      log().error("Error indexing tweet " + tweetId, e);
      e.printStackTrace();
    }
  }
}
