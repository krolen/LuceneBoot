package my.test.ws;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.nio.ByteBuffer;

/**
 * Created by kkulagin on 3/29/2016.
 */
public class MyBytesHandler extends BinaryWebSocketHandler {

  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
    ByteBuffer payload = message.getPayload();
    long tweetId = payload.getLong();
    long time = payload.getLong();
    String content = payload.asCharBuffer().toString();
    System.out.println(tweetId + ":" + time + ":" + content);
  }



}