package org.example.pro3assignment2.repository;

import org.example.pro3assignment2.model.AnimalRecord;

import java.util.List;

public interface AnimalRepository {

    // All animals that had a part in one of the trays, the product was made from
    List<AnimalRecord> findByProductId(long productId);

}
