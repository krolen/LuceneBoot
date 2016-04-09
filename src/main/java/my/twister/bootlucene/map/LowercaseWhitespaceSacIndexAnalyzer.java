package my.twister.bootlucene.map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;

/**
 * Created by kkulagin on 4/8/2016.
 */
public class LowercaseWhitespaceSacIndexAnalyzer extends Analyzer {

  private static final int FLAGS =
      WordDelimiterFilter.GENERATE_WORD_PARTS |
          WordDelimiterFilter.GENERATE_NUMBER_PARTS |
          WordDelimiterFilter.PRESERVE_ORIGINAL;

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {

    Tokenizer tokenizer = new WhitespaceSacTokenizer();
    TokenStream filter = tokenizer;
    filter = new SpecialTokenFilter(filter);
    filter = new WordDelimiterFilter(filter, FLAGS, null);
    filter = new LowerCaseFilter(filter);
    return new TokenStreamComponents(tokenizer, filter);
  }

}
