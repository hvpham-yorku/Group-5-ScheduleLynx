package ca.yorku.eecs2311.schedulelynx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScheduleLynxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleLynxApplication.class, args);
        showSpringAddress();
    }

    private static void showSpringAddress() {

      var spectrum = new String[6];
      spectrum[0] = "\u001B[38;5;196m"; // Red
      spectrum[1] = "\u001B[38;5;214m"; // Orange
      spectrum[2] = "\u001B[38;5;226m"; // Yellow
      spectrum[3] = "\u001B[38;5;46m";  // Green
      spectrum[4] = "\u001B[38;5;33m";  // Blue
      spectrum[5] = "\u001B[38;5;170m"; // Purple

      int color = 0;
      StringBuilder serverName = new StringBuilder();
      for (char c : "SpringBoot Server".toCharArray()) {
        if (!Character.isWhitespace(c)) serverName.append(spectrum[color++]);
        serverName.append(c);
        color %= spectrum.length;
      }
      serverName.append("\u001B[0m");

      System.out.println('\n' + serverName.toString() + " now running at: http://localhost:8080\n");
    }

}
