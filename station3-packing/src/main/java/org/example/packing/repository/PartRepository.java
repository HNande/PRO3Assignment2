package org.example.packing.repository;

import org.example.packing.model.PartRecord;
import java.util.List;

public interface PartRepository {

    // Parts of this type that aren't packed yet, oldest trays first
    List<PartRecord> findAvailable(String partType, int limit);

    void assignToProduct(int partId, int productId);

}
