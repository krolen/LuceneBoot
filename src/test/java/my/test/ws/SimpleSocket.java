package my.test.ws;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by kkulagin on 3/29/2016.
 */
@WebSocket(maxTextMessageSize = 64 * 1024, maxIdleTime = 6 * 60 * 60 * 1000)
public class SimpleSocket {

  private final CountDownLatch closeLatch;
  private final CountDownLatch openLatch;

  @SuppressWarnings("unused")
  private Session session;

  public SimpleSocket() {
    this.closeLatch = new CountDownLatch(1);
    this.openLatch = new CountDownLatch(1);
  }

  public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
    return this.closeLatch.await(duration, unit);
  }

  @OnWebSocketClose
  public void onClose(int statusCode, String reason) {
    System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
    this.session = null;
    this.closeLatch.countDown();
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    System.out.printf("Got connect: %s%n", session);
    this.session = session;
    openLatch.countDown();
  }

  @OnWebSocketMessage
  public void onMessage(String msg) {
    System.out.printf("Got msg: %s%n", msg);
  }

  public Session getSession() {
    return session;
  }

  public CountDownLatch getOpenLatch() {
    return openLatch;
  }
}