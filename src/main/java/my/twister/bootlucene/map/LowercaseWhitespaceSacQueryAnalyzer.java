package my.twister.bootlucene.map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;

/**
 * Created by kkulagin on 4/8/2016.
 */
public class LowercaseWhitespaceSacQueryAnalyzer extends Analyzer {

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {

    Tokenizer tokenizer = new WhitespaceSacTokenizer();
    TokenStream filter = tokenizer;
    filter = new SpecialTokenFilter(filter);
    filter = new LowerCaseFilter(filter);
    return new TokenStreamComponents(tokenizer, filter);
  }

}
