package my.twister.bootlucene;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "my.twister")
@EnableScheduling
//@IntegrationComponentScan
//@EnableWebSocket
public class LuceneApplication {

  @Bean
  public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory(@Value("${jetty.threadPool.maxThreads:100}") final String maxThreads,
                                                                                   @Value("${jetty.threadPool.minThreads:10}") final String minThreads,
                                                                                   @Value("${jetty.threadPool.idleTimeout:60000}") final String idleTimeout) {
    Integer port = Integer.valueOf(System.getProperty("server.port","8880"));
    final JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory(port);
    factory.addServerCustomizers(new JettyServerCustomizer() {
      @Override
      public void customize(final Server server) {
        // Tweak the connection pool used by Jetty to handle incoming HTTP connections
        final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
        threadPool.setMaxThreads(Integer.valueOf(maxThreads));
        threadPool.setMinThreads(Integer.valueOf(minThreads));
        threadPool.setIdleTimeout(Integer.valueOf(idleTimeout));
      }
    });
    return factory;
  }

  public static void main(String[] args) {
    int thisAppNumber = Integer.parseInt(System.getProperty("thisAppNumber"));
    int port = 8880 + thisAppNumber;
    System.setProperty("server.port", String.valueOf(port));
    int managementPort = 8890 + thisAppNumber;
    System.setProperty("management.port", String.valueOf(managementPort));
    SpringApplication.run(LuceneApplication.class, args);
  }


}

