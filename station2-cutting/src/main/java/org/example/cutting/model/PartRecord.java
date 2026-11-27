package org.example.cutting.model;

public record PartRecord(
    int id,
    String partType,
    double weightKg,
    String animalRegNo,
    int trayId
) {
}
