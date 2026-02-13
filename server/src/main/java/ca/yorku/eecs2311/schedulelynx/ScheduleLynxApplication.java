package ca.yorku.eecs2311.schedulelynx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScheduleLynxApplication {

  public static void main(String[] args) {
    SpringApplication.run(ScheduleLynxApplication.class, args);
    System.out.println("SpringBoot Server now running at: http://localhost:8080");
  }
}
