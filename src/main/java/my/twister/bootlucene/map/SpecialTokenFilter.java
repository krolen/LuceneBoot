package my.twister.bootlucene.map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by kkulagin on 4/8/2016.
 */
public final class SpecialTokenFilter extends TokenFilter {

  private static final char[] leadingChars = new char[] { '@', '#' };
  private static final char[] trailingChars = new char[] { ':', ';', '!', '?', ',', '.', '<', '>'};

  private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
  private final PositionIncrementAttribute posIncAttr = addAttribute(PositionIncrementAttribute.class);

  private Queue<String> queue = new LinkedList<>();

  public SpecialTokenFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {

    while (!queue.isEmpty()) {
      String token = queue.poll();
      if (!token.isEmpty()) {
        termAttr.copyBuffer(token.toCharArray(), 0, token.length());
        termAttr.setLength(token.length());
        posIncAttr.setPositionIncrement(1);
        return true;
      }
    }

    if (!input.incrementToken()) {
      return false;
    }

    char[] buffer = termAttr.buffer();
    int length = termAttr.length();

    if (isLeadingChar(buffer[0])) {
      int trailingCharAt = trailingCharAt(buffer, length);
      if (trailingCharAt < length) {
        termAttr.setLength(trailingCharAt);
        queue.offer(String.valueOf(buffer, trailingCharAt, length - trailingCharAt));
      }
    }

    return true;
  }

  private boolean isLeadingChar(char ch) {
    return contains(leadingChars, ch);
  }

  private int trailingCharAt(char[] buffer, int length) {
    int idx = length;
    for (int i = length -1; i > 0; --i) {
      if (!contains(trailingChars, buffer[i])) {
        break;
      }
      idx = i;
    }
    return idx;
  }

  private boolean contains(char[] arr, char ch) {

    for (int i = 0; i < arr.length; ++i) {
      if (arr[i] == ch) {
        return true;
      }
    }
    return false;
  }
}
