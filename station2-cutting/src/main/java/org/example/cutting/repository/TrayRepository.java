package org.example.cutting.repository;

import org.example.cutting.model.TrayRecord;
import java.util.List;
import java.util.Optional;

public interface TrayRepository {

    Optional<TrayRecord> findById(int id);

    // The tray currently being filled with this type of part, if any
    Optional<TrayRecord> findOpenByPartType(String partType);

    // A new, empty, open tray
    TrayRecord create(String partType, double maxWeightKg);

    void close(int id);

    // Closed trays station 3 hasn't received yet
    List<Integer> findClosedNotForwardedIds();

    void markForwarded(int id);

    // All trays that contain a part of the animal
    List<Integer> findIdsByAnimalRegNo(String regNo);

}
