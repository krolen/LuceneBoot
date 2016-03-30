package my.test.ws;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by kkulagin on 3/29/2016.
 */
public class MyBytesHandler extends BinaryWebSocketHandler {

  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
    ByteBuffer payload = message.getPayload();
    long tweetId = payload.getLong();
    long time = payload.getLong();
    byte[] array = payload.array();
    String content = new String(array, 16, array.length - 16, Charset.forName("UTF-8"));
    System.out.println(tweetId + ":" + time + ":" + content);
  }



}