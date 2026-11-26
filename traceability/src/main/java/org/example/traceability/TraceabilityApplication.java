package org.example.traceability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Traceability: used from outside the slaughterhouse, e.g. to recall all products
// that might contain parts of an animal. The data comes from the stations over gRPC.
@SpringBootApplication
public class TraceabilityApplication {

  public static void main(String[] args) {
    SpringApplication.run(TraceabilityApplication.class, args);
  }

}
