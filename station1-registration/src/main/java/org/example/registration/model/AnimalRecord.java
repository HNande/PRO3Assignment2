package org.example.registration.model;

import java.time.LocalDate;

public record AnimalRecord(
    String regNo,
    String species, double weightKg,
    LocalDate arrivalDate,
    String origin
) {
}
