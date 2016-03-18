package my.test.rest;

import my.test.service.LuceneService;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author kkulagin
 * @since 07.02.2016
 */
@RestController
public class LuceneController {

  @Autowired
  private LuceneService luceneService;

  @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public TopDocs search(@RequestParam(value = "q") String query, @RequestParam(value = "max", required = false) Integer count) throws QueryNodeException, IOException {
    Query q = luceneService.parse(query);
    return luceneService.search(q, count);
  }
}
