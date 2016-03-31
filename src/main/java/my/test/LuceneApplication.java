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
    int thisAppNumber = Integer.parseInt(System.getProperty("thisAppNumber"));
    int port = 8880 + thisAppNumber;
    System.setProperty("server.port", String.valueOf(port));
    int managementPort = 8890 + thisAppNumber;
    System.setProperty("management.port", String.valueOf(managementPort));
    SpringApplication.run(LuceneApplication.class, args);
  }


}

