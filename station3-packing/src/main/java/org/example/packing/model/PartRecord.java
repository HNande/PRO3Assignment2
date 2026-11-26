package org.example.packing.model;

public record PartRecord(
    int id,
    String partType,
    double weightKg,
    int trayId
) {
}
