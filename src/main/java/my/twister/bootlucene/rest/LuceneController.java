package my.twister.bootlucene.rest;

import my.twister.bootlucene.service.LuceneService;
import my.twister.bootlucene.utils.LogAware;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author kkulagin
 * @since 07.02.2016
 */
@RestController
public class LuceneController implements LogAware {

  @Autowired
  private LuceneService luceneService;

  @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public TopDocs search(@RequestParam(value = "q") String query, @RequestParam(value = "max", required = false) Integer count) throws QueryNodeException, IOException {
    Query q = luceneService.parse(query);
    return luceneService.search(q, count);
  }


  @RequestMapping(value = "/put/{tweetId}/{time}", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void put(@PathVariable("tweetId") long tweetId, @PathVariable("time") Integer time, @RequestBody String body) {
    try {
      luceneService.index(tweetId, time, body);
    } catch (IOException e) {
      log().error("Error indexing tweet " + tweetId, e);
      e.printStackTrace();
    }
  }
}
