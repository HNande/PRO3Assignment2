package org.example.cutting.model;

// Station 2 only needs to know which animals have arrived from station 1
public record AnimalRecord(
    String regNo,
    String species
) {
}
