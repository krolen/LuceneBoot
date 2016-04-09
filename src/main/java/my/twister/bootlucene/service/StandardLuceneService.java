package my.twister.bootlucene.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.springframework.stereotype.Service;

/**
 * Created by kkulagin on 4/8/2016.
 */
//@Service
public class StandardLuceneService extends LuceneService {
  @Override
  protected Analyzer analyzer() {
    return new StandardAnalyzer();
  }

  @Override
  protected Analyzer queryAnalyzer() {
    return analyzer();
  }
}
