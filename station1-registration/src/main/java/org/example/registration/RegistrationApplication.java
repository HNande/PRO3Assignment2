package org.example.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Station 1: animals arrive, are weighed and registered.
// Scheduling is used to resend animals station 2 didn't receive (e.g. network down).
@SpringBootApplication
@EnableScheduling
public class RegistrationApplication {

  public static void main(String[] args) {
    SpringApplication.run(RegistrationApplication.class, args);
  }

}
