package org.example.cutting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Station 2: animals are cut into parts, each part is weighed, registered and put into a tray.
// Scheduling is used to resend trays station 3 didn't receive (e.g. network down).
@SpringBootApplication
@EnableScheduling
public class CuttingApplication {

  public static void main(String[] args) {
    SpringApplication.run(CuttingApplication.class, args);
  }

}
