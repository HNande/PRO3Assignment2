package org.example.packing.service;

import org.example.packing.model.PartRecord;
import org.example.packing.model.ProductRecord;
import java.util.List;

public interface PackingService {

    // A closed tray handed over from station 2. Receiving the same tray twice is fine.
    void receiveTray(int trayId, String partType, List<PartRecord> parts);

    // A package with partCount parts of the same type, e.g. for repackaging in supermarkets.
    // Throws IllegalArgumentException for bad input, IllegalStateException if there aren't enough parts.
    ProductRecord packPackage(String partType, int partCount);

    // Half an animal: one of every expected part type, not necessarily from the same animal.
    // Throws IllegalStateException if one of the parts isn't available.
    ProductRecord packHalfAnimal();

    // Throws NotFoundException.
    ProductRecord getProduct(int productId);

    List<Integer> getTrayIdsInProduct(int productId);

    List<ProductRecord> getProductsForTrays(List<Integer> trayIds);

}
