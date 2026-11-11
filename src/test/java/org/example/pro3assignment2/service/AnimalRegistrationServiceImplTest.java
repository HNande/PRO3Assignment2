package org.example.pro3assignment2.service;

import org.example.pro3assignment2.model.AnimalRecord;
import org.example.pro3assignment2.repository.AnimalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// No Spring and no database here — the service only knows the repository
// interface, so we can give it a fake one.
class AnimalRegistrationServiceImplTest {

    private AnimalRegistrationService service;
    private List<AnimalRecord> storedAnimals;

    @BeforeEach
    void setUp() {
        storedAnimals = new ArrayList<>();

        AnimalRepository fakeRepository = new AnimalRepository() {
            @Override
            public List<AnimalRecord> findByProductId(long productId) {
                return List.of();
            }

            @Override
            public Optional<AnimalRecord> findByRegistrationNumber(String regNo) {
                return storedAnimals.stream()
                        .filter(a -> a.regNo().equals(regNo))
                        .findFirst();
            }

            @Override
            public List<AnimalRecord> findByArrivalDate(LocalDate date) {
                return storedAnimals.stream()
                        .filter(a -> a.arrivalDate().equals(date))
                        .toList();
            }

            @Override
            public List<AnimalRecord> findByOrigin(String origin) {
                return storedAnimals.stream()
                        .filter(a -> a.origin().equals(origin))
                        .toList();
            }

            @Override
            public void save(AnimalRecord animal) {
                storedAnimals.add(animal);
            }
        };

        service = new AnimalRegistrationServiceImpl(fakeRepository);
    }

    @Test
    void registerAnimalSavesAndReturnsAnimal() {
        AnimalRecord animal = new AnimalRecord("A-2001", "Pig", 95.0, LocalDate.of(2026, 11, 1), "Test Farm");
        AnimalRecord result = service.registerAnimal(animal);

        assertEquals(animal, result);
        assertEquals(1, storedAnimals.size());
    }

    @Test
    void getAnimalByRegistrationNumberFindsExistingAnimal() {
        AnimalRecord animal = new AnimalRecord("A-2001", "Pig", 95.0, LocalDate.of(2026, 11, 1), "Test Farm");
        storedAnimals.add(animal);

        Optional<AnimalRecord> result = service.getAnimalByRegistrationNumber("A-2001");

        assertTrue(result.isPresent());
        assertEquals("A-2001", result.get().regNo());
    }

    @Test
    void getAnimalByRegistrationNumberReturnsEmptyForUnknown() {
        Optional<AnimalRecord> result = service.getAnimalByRegistrationNumber("NOPE");

        assertTrue(result.isEmpty());
    }

    @Test
    void getAnimalByRegistrationNumberTrimsAndUppercasesInput() {
        AnimalRecord animal = new AnimalRecord("A-2001", "Pig", 95.0, LocalDate.of(2026, 11, 1), "Test Farm");
        storedAnimals.add(animal);

        Optional<AnimalRecord> result = service.getAnimalByRegistrationNumber("  a-2001  ");

        assertTrue(result.isPresent());
        assertEquals("A-2001", result.get().regNo());
    }

    @Test
    void getAnimalsByArrivalDateReturnsMatchingAnimals() {
        storedAnimals.add(new AnimalRecord("A-2001", "Pig", 95.0, LocalDate.of(2026, 11, 1), "Farm A"));
        storedAnimals.add(new AnimalRecord("A-2002", "Pig", 100.0, LocalDate.of(2026, 11, 1), "Farm B"));
        storedAnimals.add(new AnimalRecord("A-2003", "Pig", 110.0, LocalDate.of(2026, 11, 2), "Farm A"));

        List<AnimalRecord> result = service.getAnimalsByArrivalDate(LocalDate.of(2026, 11, 1));

        assertEquals(2, result.size());
    }

    @Test
    void getAnimalsByArrivalDateReturnsEmptyWhenNoMatch() {
        List<AnimalRecord> result = service.getAnimalsByArrivalDate(LocalDate.of(2020, 1, 1));

        assertTrue(result.isEmpty());
    }

    @Test
    void getAnimalsByOriginReturnsMatchingAnimals() {
        storedAnimals.add(new AnimalRecord("A-2001", "Pig", 95.0, LocalDate.of(2026, 11, 1), "Sundsvik Farm"));
        storedAnimals.add(new AnimalRecord("A-2002", "Pig", 100.0, LocalDate.of(2026, 11, 1), "Sundsvik Farm"));
        storedAnimals.add(new AnimalRecord("A-2003", "Pig", 110.0, LocalDate.of(2026, 11, 2), "Gävle Farm"));

        List<AnimalRecord> result = service.getAnimalsByOrigin("Sundsvik Farm");

        assertEquals(2, result.size());
    }

    @Test
    void getAnimalsByOriginReturnsEmptyWhenNoMatch() {
        List<AnimalRecord> result = service.getAnimalsByOrigin("Unknown Farm");

        assertTrue(result.isEmpty());
    }
}
