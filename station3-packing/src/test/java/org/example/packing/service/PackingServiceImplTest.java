package org.example.packing.service;

import org.example.packing.model.PartRecord;
import org.example.packing.model.ProductRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// The real service and repositories against H2 (schema.sql + data.sql).
// @Transactional rolls back after every test so they don't affect each other.
@SpringBootTest
@Transactional
class PackingServiceImplTest {

    @Autowired
    private PackingService packingService;

    @Test
    void packageIsMadeFromUnpackedPartsOfOneType() {
        // the seed data has two loins (parts 6 and 8 in tray 3) that aren't packed
        ProductRecord product = packingService.packPackage("loin", 2);

        assertEquals(ProductRecord.PACKAGE, product.type());
        assertEquals(List.of(6, 8), product.partIds());
        assertEquals(List.of(3), product.trayIds());
        assertTrue(product.id() >= 4, "ids continue after the seed data");
    }

    @Test
    void aPartIsOnlyPackedOnce() {
        packingService.packPackage("LOIN", 2);

        assertThrows(IllegalStateException.class, () -> packingService.packPackage("LOIN", 1));
    }

    @Test
    void notEnoughPartsForPackage() {
        assertThrows(IllegalStateException.class, () -> packingService.packPackage("LOIN", 3));
    }

    @Test
    void invalidPackageRequest() {
        assertThrows(IllegalArgumentException.class, () -> packingService.packPackage("LOIN", 0));
        assertThrows(IllegalArgumentException.class, () -> packingService.packPackage(" ", 1));
    }

    @Test
    void receivedTrayCanBePacked() {
        packingService.receiveTray(5, "HAM", List.of(
                new PartRecord(9, "HAM", 12.0, 5),
                new PartRecord(10, "HAM", 12.5, 5)));

        ProductRecord product = packingService.packPackage("HAM", 2);

        assertEquals(List.of(9, 10), product.partIds());
        assertEquals(List.of(5), product.trayIds());
    }

    @Test
    void receivingSameTrayTwiceIsIgnored() {
        List<PartRecord> parts = List.of(new PartRecord(9, "HAM", 12.0, 5));
        packingService.receiveTray(5, "HAM", parts);

        assertDoesNotThrow(() -> packingService.receiveTray(5, "HAM", parts));
        packingService.packPackage("HAM", 1);
        assertThrows(IllegalStateException.class, () -> packingService.packPackage("HAM", 1));
    }

    @Test
    void trayWithMixedPartsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> packingService.receiveTray(5, "HAM",
                List.of(new PartRecord(9, "RIBS", 8.0, 5))));
    }

    @Test
    void halfAnimalHasOneOfEveryExpectedPartFromDifferentTrays() {
        packingService.receiveTray(5, "HAM", List.of(new PartRecord(9, "HAM", 12.0, 5)));
        packingService.receiveTray(6, "RIBS", List.of(new PartRecord(10, "RIBS", 8.0, 6)));

        ProductRecord product = packingService.packHalfAnimal();

        assertEquals(ProductRecord.HALF_ANIMAL, product.type());
        assertEquals(List.of(6, 9, 10), product.partIds());
        assertEquals(List.of(3, 5, 6), product.trayIds());
    }

    @Test
    void halfAnimalFailsWhenAPartIsMissing() {
        // no unpacked ham or ribs in the seed data
        assertThrows(IllegalStateException.class, () -> packingService.packHalfAnimal());
    }

    @Test
    void getProduct() {
        ProductRecord product = packingService.getProduct(3);

        assertEquals(ProductRecord.HALF_ANIMAL, product.type());
        assertEquals(List.of(2, 3, 4), product.trayIds());
        assertEquals(List.of(3, 5, 7), product.partIds());
        assertThrows(NotFoundException.class, () -> packingService.getProduct(999));
    }

    @Test
    void traceabilityLookups() {
        assertEquals(List.of(1), packingService.getTrayIdsInProduct(1));
        assertTrue(packingService.getTrayIdsInProduct(999).isEmpty());

        List<ProductRecord> products = packingService.getProductsForTrays(List.of(2, 4));
        assertEquals(List.of(2, 3), products.stream().map(ProductRecord::id).toList());
        assertTrue(packingService.getProductsForTrays(List.of()).isEmpty());
    }
}
