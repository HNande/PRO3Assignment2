package org.example.cutting.service;

import org.example.cutting.forwarding.TrayForwarder;
import org.example.cutting.model.AnimalRecord;
import org.example.cutting.model.PartRecord;
import org.example.cutting.model.TrayRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// The real service and repositories against H2 (schema.sql + data.sql).
// Only the forwarding to station 3 is faked.
// @Transactional rolls back after every test so they don't affect each other.
@SpringBootTest
@Transactional
class CuttingServiceImplTest {

    @Autowired
    private CuttingService cuttingService;

    @MockitoBean
    private TrayForwarder trayForwarder;

    @Test
    void receivedAnimalCanBeCut() {
        cuttingService.receiveAnimal(new AnimalRecord("a-2001", "Cow"));

        PartRecord part = cuttingService.cutPart("A-2001", "brisket", 10.0);

        assertEquals("A-2001", part.animalRegNo());
        assertEquals("BRISKET", part.partType());
        assertTrue(part.id() >= 9, "ids continue after the seed data");
    }

    @Test
    void receivingSameAnimalTwiceIsFine() {
        cuttingService.receiveAnimal(new AnimalRecord("A-2001", "Cow"));
        assertDoesNotThrow(() -> cuttingService.receiveAnimal(new AnimalRecord("A-2001", "Cow")));
    }

    @Test
    void unknownAnimalCantBeCut() {
        assertThrows(NotFoundException.class, () -> cuttingService.cutPart("NOPE", "HAM", 10.0));
    }

    @Test
    void invalidWeightIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> cuttingService.cutPart("A-1001", "HAM", 0));
        assertThrows(IllegalArgumentException.class, () -> cuttingService.cutPart("A-1001", "HAM", 51.0));
    }

    @Test
    void partsOfSameTypeShareATrayUntilItIsFull() {
        PartRecord first = cuttingService.cutPart("A-1001", "SHOULDER", 20.0);
        PartRecord second = cuttingService.cutPart("A-1002", "SHOULDER", 20.0);

        assertEquals(first.trayId(), second.trayId());
        assertEquals(40.0, cuttingService.getTray(first.trayId()).currentWeightKg(), 0.001);
        verify(trayForwarder, never()).forward(anyInt());

        // 40 + 20 > 50: the tray is closed, sent to station 3 and a new one is started
        PartRecord third = cuttingService.cutPart("A-1003", "SHOULDER", 20.0);

        assertNotEquals(first.trayId(), third.trayId());
        assertTrue(cuttingService.getTray(first.trayId()).closed());
        assertFalse(cuttingService.getTray(third.trayId()).closed());
        verify(trayForwarder).forward(first.trayId());
    }

    @Test
    void aTrayOnlyHoldsOneTypeOfPart() {
        PartRecord ham = cuttingService.cutPart("A-1001", "BELLY", 5.0);
        PartRecord ribs = cuttingService.cutPart("A-1001", "CHEEK", 1.0);

        assertNotEquals(ham.trayId(), ribs.trayId());
        assertEquals("BELLY", cuttingService.getTray(ham.trayId()).partType());
        assertEquals("CHEEK", cuttingService.getTray(ribs.trayId()).partType());
    }

    @Test
    void closingATraySendsItToStation3() {
        PartRecord part = cuttingService.cutPart("A-1001", "SHOULDER", 5.0);

        TrayRecord tray = cuttingService.closeTray(part.trayId());

        assertTrue(tray.closed());
        verify(trayForwarder).forward(part.trayId());

        // the next part of the same type gets a new tray
        assertNotEquals(part.trayId(), cuttingService.cutPart("A-1002", "SHOULDER", 5.0).trayId());
    }

    @Test
    void closingUnknownTrayFails() {
        assertThrows(NotFoundException.class, () -> cuttingService.closeTray(999));
    }

    @Test
    void traysForAnimal() {
        assertEquals(List.of(3, 4), cuttingService.getTrayIdsForAnimal(" a-1003 "));
        assertTrue(cuttingService.getTrayIdsForAnimal("NOPE").isEmpty());
    }

    @Test
    void animalsInTrays() {
        assertEquals(List.of("A-1001", "A-1002"), cuttingService.getAnimalRegNosInTrays(List.of(1, 2)));
        assertEquals(List.of("A-1001", "A-1002", "A-1003"), cuttingService.getAnimalRegNosInTrays(List.of(2, 3, 4)));
        assertTrue(cuttingService.getAnimalRegNosInTrays(List.of()).isEmpty());
    }
}
