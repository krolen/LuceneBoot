package my.twister.bootlucene.ws;

import com.google.common.primitives.Longs;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Test;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Created by kkulagin on 3/29/2016.
 */
public class WSClientTests {

  private WebSocketClient client;
  private SimpleSocket socket;

  public void setUp(String destUri) throws Exception {
    System.setProperty("org.eclipse.jetty.websocket.LEVEL", "INFO");

    client = new WebSocketClient();

    socket = new SimpleSocket();
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
    setUp("ws://localhost:8891/textSaveTweet");

    Session session = socket.getSession();
    RemoteEndpoint remote = session.getRemote();
//    ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);

    long l = System.nanoTime();
    for (int i = 5; i < 106; i++) {
      String msg = "this is my super mega text" + i;
      remote.sendString((long)i + "|" + (long)i  + "|" + msg);
    }
    System.out.println(System.nanoTime() - l);
  }

  @Test
  public void testText2() throws Exception {
    setUp("ws://localhost:8881/textSaveTweet2");

    Session session = socket.getSession();
    RemoteEndpoint remote = session.getRemote();
//    ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);

    long l = System.nanoTime();
    for (long i = 5; i < 106; i++) {
      String msg = "this is my super mega text" + i;
      remote.sendString(i + "|" + i + "|" + msg);
    }
    System.out.println(System.nanoTime() - l);
  }

  @Test // TODO: 3/30/2016 Surprise - this one works slower....
  public void testBinary() throws Exception {
    setUp("ws://localhost:8080/bytesSaveTweet");

    Session session = socket.getSession();
    RemoteEndpoint remote = session.getRemote();
    ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);
    long l = System.nanoTime();
    for (int i = 5; i < 106; i++) {
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
