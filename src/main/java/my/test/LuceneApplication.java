package my.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "my.test")
public class LuceneApplication {

  public static void main(String[] args) {
    SpringApplication.run(LuceneApplication.class, args);
  }

}