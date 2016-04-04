package my.twister.bootlucene.ws;

import com.google.common.base.Splitter;
import my.twister.bootlucene.service.LuceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by kkulagin on 3/29/2016.
 */
public class MyHandler extends TextWebSocketHandler {

  private static final Splitter SPLITTER = Splitter.on('|').limit(3);

  @Autowired
  private LuceneService luceneService;

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
    String payload = message.getPayload();
    Iterator<String> strings = SPLITTER.split(payload).iterator();
    long tweetId = Long.parseLong(strings.next());
    long time = Long.parseLong(strings.next());
    String content = strings.next();
//    System.out.println(tweetId + ":" + time + ":" + content);
    try {
      luceneService.index(tweetId, time, content);
    } catch (Exception e) {
      e.printStackTrace();
      session.sendMessage(new TextMessage("Error: " + e.getMessage()));
    }
  }

//  @Override
//  public void handleTextMessage(WebSocketSession session, TextMessage message) {
//    byte[] bytes = message.asBytes();
//    long tweetId = Longs.fromBytes(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
//    long time = Longs.fromBytes(bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]);
//    String content = new String(bytes, 16, bytes.length - 16, Charset.forName("UTF-8"));
//    System.out.println(tweetId + ":" + time + ":" + content);
//  }
//
}