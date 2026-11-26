package org.example.registration.repository;

import org.example.registration.model.AnimalRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnimalRepository {

    // Find a specific animal by its registration number
    Optional<AnimalRecord> findByRegistrationNumber(String regNo);

    // Find several animals at once (unknown registration numbers are skipped)
    List<AnimalRecord> findByRegistrationNumbers(List<String> regNos);

    // All animals that arrived on a specific date
    List<AnimalRecord> findByArrivalDate(LocalDate date);

    // All animals from a specific origin
    List<AnimalRecord> findByOrigin(String origin);

    // Register a new animal (not yet forwarded to station 2)
    void save(AnimalRecord animal);

    // Animals station 2 hasn't received yet
    List<AnimalRecord> findNotForwarded();

    void markForwarded(String regNo);

}
