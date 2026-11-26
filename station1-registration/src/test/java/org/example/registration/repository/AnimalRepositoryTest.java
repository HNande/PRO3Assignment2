package org.example.registration.repository;

import org.example.registration.model.AnimalRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Runs the real SQL against H2 filled with schema.sql + data.sql.
// @Transactional rolls back after every test so they don't affect each other.
@SpringBootTest
@Transactional
class AnimalRepositoryTest {

    @Autowired
    private AnimalRepository animalRepository;

    @Test
    void findByRegistrationNumber() {
        AnimalRecord animal = animalRepository.findByRegistrationNumber("A-1003").orElseThrow();
        assertEquals("Gävle Farm", animal.origin());
        assertEquals(LocalDate.of(2026, 10, 2), animal.arrivalDate());
    }

    @Test
    void findByRegistrationNumbersSkipsUnknown() {
        List<AnimalRecord> animals = animalRepository.findByRegistrationNumbers(List.of("A-1002", "A-1001", "NOPE"));
        assertEquals(List.of("A-1001", "A-1002"), animals.stream().map(AnimalRecord::regNo).toList());
    }

    @Test
    void findByRegistrationNumbersWithEmptyList() {
        assertTrue(animalRepository.findByRegistrationNumbers(List.of()).isEmpty());
    }

    @Test
    void findByArrivalDate() {
        assertEquals(2, animalRepository.findByArrivalDate(LocalDate.of(2026, 10, 1)).size());
    }

    @Test
    void findByOrigin() {
        assertEquals(2, animalRepository.findByOrigin("Sundsvik Farm").size());
    }

    @Test
    void newAnimalIsNotForwardedUntilMarked() {
        animalRepository.save(new AnimalRecord("A-9999", "Cow", 500.0, LocalDate.of(2026, 11, 26), "Test Farm"));
        assertEquals(List.of("A-9999"), animalRepository.findNotForwarded().stream().map(AnimalRecord::regNo).toList());

        animalRepository.markForwarded("A-9999");
        assertTrue(animalRepository.findNotForwarded().isEmpty());
    }
}
