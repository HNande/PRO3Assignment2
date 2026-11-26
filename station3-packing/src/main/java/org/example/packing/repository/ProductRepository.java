package org.example.packing.repository;

import org.example.packing.model.ProductRecord;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    // Registers a new, empty product and returns its id
    int create(String type);

    // Registers that the product has parts from the tray
    void addTray(int productId, int trayId);

    Optional<ProductRecord> findById(int productId);

    List<Integer> findTrayIds(int productId);

    // All products made with parts from one of the trays
    List<ProductRecord> findByTrayIds(List<Integer> trayIds);

}
