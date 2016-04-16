package my.twister.bootlucene.ws;

import com.google.common.base.Splitter;
import com.google.common.primitives.Longs;
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
    byte[] tweetId = Longs.toByteArray(Long.parseLong(strings.next()));
    long time = Long.parseLong(strings.next());
    String content = strings.next();
    try {
      luceneService.index(tweetId, time, content);
    } catch (Exception e) {
      e.printStackTrace();
      session.sendMessage(new TextMessage("Error: " + e.getMessage()));
    }
  }


}