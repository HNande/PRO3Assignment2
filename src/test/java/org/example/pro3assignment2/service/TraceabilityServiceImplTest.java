package org.example.pro3assignment2.service;

import org.example.pro3assignment2.model.AnimalRecord;
import org.example.pro3assignment2.model.ProductRecord;
import org.example.pro3assignment2.repository.AnimalRepository;
import org.example.pro3assignment2.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// No Spring and no database here - because the service only knows the
// repository interfaces, we can just give it fake ones.
class TraceabilityServiceImplTest {

    private TraceabilityService service;
    private String lastRegNoSearched;

    @BeforeEach
    void setUp() {
        AnimalRepository fakeAnimals = productId -> productId == 1
                ? List.of(new AnimalRecord("A-1", "Pig", 100, LocalDate.of(2026, 10, 1)))
                : List.of();

        ProductRepository fakeProducts = regNo -> {
            lastRegNoSearched = regNo;
            return List.of(new ProductRecord(7, "PACKAGE"));
        };

        service = new TraceabilityServiceImpl(fakeAnimals, fakeProducts);
    }

    @Test
    void getAnimalsInProductReturnsAnimalsFromRepository() {
        List<AnimalRecord> animals = service.getAnimalsInProduct(1);
        assertEquals(1, animals.size());
        assertEquals("A-1", animals.get(0).regNo());
    }

    @Test
    void unknownProductGivesEmptyList() {
        assertTrue(service.getAnimalsInProduct(99).isEmpty());
    }

    @Test
    void regNoIsCleanedUpBeforeSearching() {
        service.getProductsForAnimal("  a-1001 ");
        assertEquals("A-1001", lastRegNoSearched);
    }
}
