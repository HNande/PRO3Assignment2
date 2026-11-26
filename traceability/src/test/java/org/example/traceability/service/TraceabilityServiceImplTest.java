package org.example.traceability.service;

import org.example.traceability.model.AnimalRecord;
import org.example.traceability.model.ProductRecord;
import org.example.traceability.station.CuttingClient;
import org.example.traceability.station.PackingClient;
import org.example.traceability.station.RegistrationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// No Spring and no network here - because the service only knows the
// station client interfaces, we can just give it fake ones.
class TraceabilityServiceImplTest {

    private TraceabilityService service;
    private String lastRegNoSearched;
    private final List<String> callsToStation1 = new ArrayList<>();

    @BeforeEach
    void setUp() {
        RegistrationClient fakeStation1 = regNos -> {
            callsToStation1.addAll(regNos);
            return regNos.stream()
                    .map(regNo -> new AnimalRecord(regNo, "Pig", 100, LocalDate.of(2026, 10, 1), "Test Farm"))
                    .toList();
        };

        CuttingClient fakeStation2 = new CuttingClient() {
            @Override
            public List<Integer> getTrayIdsForAnimal(String regNo) {
                lastRegNoSearched = regNo;
                return regNo.equals("A-1001") ? List.of(1, 2) : List.of();
            }

            @Override
            public List<String> getAnimalRegNosInTrays(List<Integer> trayIds) {
                return trayIds.equals(List.of(1)) ? List.of("A-1001", "A-1002") : List.of();
            }
        };

        PackingClient fakeStation3 = new PackingClient() {
            @Override
            public List<Integer> getTrayIdsInProduct(int productId) {
                return productId == 1 ? List.of(1) : List.of();
            }

            @Override
            public List<ProductRecord> getProductsForTrays(List<Integer> trayIds) {
                return trayIds.equals(List.of(1, 2))
                        ? List.of(new ProductRecord(1, "PACKAGE", List.of(1)), new ProductRecord(2, "PACKAGE", List.of(2)))
                        : List.of();
            }
        };

        service = new TraceabilityServiceImpl(fakeStation1, fakeStation2, fakeStation3);
    }

    @Test
    void animalsInProductAreFoundThroughItsTrays() {
        List<AnimalRecord> animals = service.getAnimalsInProduct(1);
        assertEquals(List.of("A-1001", "A-1002"), animals.stream().map(AnimalRecord::regNo).toList());
    }

    @Test
    void unknownProductGivesEmptyListWithoutAskingStation1() {
        assertTrue(service.getAnimalsInProduct(99).isEmpty());
        assertTrue(callsToStation1.isEmpty());
    }

    @Test
    void productsForAnimalAreFoundThroughItsTrays() {
        List<ProductRecord> products = service.getProductsForAnimal("A-1001");
        assertEquals(List.of(1, 2), products.stream().map(ProductRecord::id).toList());
    }

    @Test
    void unknownAnimalHasNoProducts() {
        assertTrue(service.getProductsForAnimal("NOPE").isEmpty());
    }

    @Test
    void regNoIsCleanedUpBeforeSearching() {
        service.getProductsForAnimal("  a-1001 ");
        assertEquals("A-1001", lastRegNoSearched);
    }
}
