package my.test.ws;

import com.google.common.util.concurrent.Uninterruptibles;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by kkulagin on 3/29/2016.
 */
public class WSClientTests {

  private WebSocketClient client;
  private SimpleEchoSocket socket;

  public void setUp(String destUri) throws Exception {
    System.setProperty("org.eclipse.jetty.websocket.LEVEL", "INFO");

    client = new WebSocketClient();

    socket = new SimpleEchoSocket();
    client.start();
    URI echoUri = new URI(destUri);
    ClientUpgradeRequest request = new ClientUpgradeRequest();
    System.out.printf("Connecting to : %s%n", echoUri);
    client.connect(socket, echoUri, request);
    if (!socket.getOpenLatch().await(10, TimeUnit.SECONDS)) {
      throw new RuntimeException("Cannot connect to a server!!!!");
    }
    System.out.println("Connected");
  }

  @Test
  public void testText() throws Exception {
    setUp("ws://localhost:8080/textSaveTweet");

    Session session = socket.getSession();
    RemoteEndpoint remote = session.getRemote();
//    ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);

    long l = System.nanoTime();
    for (int i = 5; i < 300006; i++) {
      String msg = "this is my super mega text" + i;
      remote.sendString((long)i + "|" + (long)i  + "|" + msg);
    }
    System.out.println(System.nanoTime() - l);
  }

  @Test
  public void testBinary() throws Exception {
    setUp("ws://localhost:8080/bytesSaveTweet");

    Session session = socket.getSession();
    RemoteEndpoint remote = session.getRemote();
    ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);
//    remote.setBatchMode(BatchMode.OFF);
    long l = System.nanoTime();
//    for (int i = 5; i < 30; i++) {
    for (int i = 5; i < 300006; i++) {
      String msg = "this is " + i;
      byte[] bytes = msg.getBytes();
      buffer.putLong(i).putLong(i).put(bytes).rewind();
      buffer.limit(bytes.length + 16);
      remote.sendBytes(buffer);
      buffer.clear();
    }
    System.out.println(System.nanoTime() - l);
  }



  @After
  public void tearDown() throws Exception {
    socket.getSession().close(StatusCode.NORMAL, "Done");
    client.stop();
  }
}
