package my.test.ws;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Created by kkulagin on 3/29/2016.
 */
public class MyHandler extends TextWebSocketHandler {

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) {
    String payload = message.getPayload();
    System.out.println(payload);
    // ...
  }

}