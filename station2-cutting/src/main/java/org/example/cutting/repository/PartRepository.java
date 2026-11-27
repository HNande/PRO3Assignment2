package org.example.cutting.repository;

import org.example.cutting.model.PartRecord;
import java.util.List;

public interface PartRepository {

    // Registers a new part and returns it with its id
    PartRecord save(String partType, double weightKg, String animalRegNo, int trayId);

    List<PartRecord> findByTrayId(int trayId);

    // Registration numbers of all animals that have a part in one of the trays
    List<String> findAnimalRegNosByTrayIds(List<Integer> trayIds);

}
