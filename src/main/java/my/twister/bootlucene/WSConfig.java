package my.twister.bootlucene;

import my.twister.bootlucene.ws.MyBytesHandler;
import my.twister.bootlucene.ws.MyHandler;
import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.time.Duration;

/**
 * Created by kkulagin on 3/29/2016.
 */
@Configuration
@EnableWebSocket
public class WSConfig implements WebSocketConfigurer {

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(tweetHandler(), "/textSaveTweet").setHandshakeHandler(handshakeHandler()).setAllowedOrigins("*");
    registry.addHandler(tweetBytesHandler(), "/bytesSaveTweet").setHandshakeHandler(handshakeHandler()).setAllowedOrigins("*");
  }

  @Bean
  public WebSocketHandler tweetHandler() {
    return new MyHandler();
  }

  @Bean
  public WebSocketHandler tweetBytesHandler() {
    return new MyBytesHandler();
  }

  @Bean
  public DefaultHandshakeHandler handshakeHandler() {
    WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
    policy.setInputBufferSize(8192 * 4);
    policy.setIdleTimeout(Duration.ofHours(6).toMillis());
    return new DefaultHandshakeHandler(new JettyRequestUpgradeStrategy(new WebSocketServerFactory(policy)));
  }
}