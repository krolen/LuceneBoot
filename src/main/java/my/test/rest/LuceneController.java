package my.test.rest;

import my.test.service.LuceneService;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kkulagin
 * @since 07.02.2016
 */
@RestController
public class LuceneController {


  @Autowired
  private LuceneService luceneService;

  @RequestMapping(value = "/search", method = RequestMethod.GET)
  public String search(@RequestParam(value = "q") String query, @RequestParam(value = "max", required = false) Integer count ) {
    new SimpleQueryParser()
    luceneService.search();
  }
}
