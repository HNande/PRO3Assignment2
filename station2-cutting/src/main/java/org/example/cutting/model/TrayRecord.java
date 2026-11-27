package org.example.cutting.model;

// A tray only holds one type of part. currentWeightKg is the sum of the parts in it.
public record TrayRecord(
    int id,
    String partType,
    double maxWeightKg,
    double currentWeightKg,
    boolean closed
) {
}
