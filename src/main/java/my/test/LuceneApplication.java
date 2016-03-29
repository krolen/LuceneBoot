package my.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@ComponentScan(basePackages = "my.test")
@EnableScheduling
//@IntegrationComponentScan
//@EnableWebSocket
public class LuceneApplication {

  public static void main(String[] args) {
    SpringApplication.run(LuceneApplication.class, args);
  }


}

