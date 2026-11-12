package org.example.pro3assignment2.repository;

import org.example.pro3assignment2.model.AnimalRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnimalRepository {

    // All animals that had a part in one of the trays, the product was made from
    List<AnimalRecord> findByProductId(int productId);

    // Find a specific animal by its registration number
    Optional<AnimalRecord> findByRegistrationNumber(String regNo);

    // All animals that arrived on a specific date
    List<AnimalRecord> findByArrivalDate(LocalDate date);

    // All animals from a specific origin
    List<AnimalRecord> findByOrigin(String origin);

    // Register a new animal
    void save(AnimalRecord animal);

}
