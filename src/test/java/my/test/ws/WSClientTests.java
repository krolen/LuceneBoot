package my.test.ws;

import com.google.common.util.concurrent.Uninterruptibles;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WriteCallback;
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

    long l = System.nanoTime();
    for (int i = 5; i < 300006; i++) {
      remote.sendString("this is " + i);
    }
    System.out.println(System.nanoTime() - l);
  }

  @Test
  public void testBinary() throws Exception {
    setUp("ws://localhost:8080/bytesSaveTweet");

    Session session = socket.getSession();
    RemoteEndpoint remote = session.getRemote();
    long l = System.nanoTime();
    for (int i = 5; i < 300006; i++) {
      String msg = "this is " + i;
      byte[] bytes = msg.getBytes();
      ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + bytes.length);
      buffer.putLong(i).putLong(i).put(bytes);
      remote.sendBytes(buffer, new WriteCallback() {
        @Override
        public void writeFailed(Throwable x) {
          x.printStackTrace();
        }

        @Override
        public void writeSuccess() {
          System.out.println("Success");
        }
      });
    }
    System.out.println(System.nanoTime() - l);
  }

  @After
  public void tearDown() throws Exception {
    socket.getSession().close(StatusCode.NORMAL, "Done");
    client.stop();
  }
}
