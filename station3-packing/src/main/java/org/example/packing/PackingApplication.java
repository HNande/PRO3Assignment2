package org.example.packing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Station 3: products are packed for distribution from the parts in the trays received from station 2.
@SpringBootApplication
public class PackingApplication {

  public static void main(String[] args) {
    SpringApplication.run(PackingApplication.class, args);
  }

}
