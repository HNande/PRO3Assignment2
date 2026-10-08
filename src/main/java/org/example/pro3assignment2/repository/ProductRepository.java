package org.example.pro3assignment2.repository;

import org.example.pro3assignment2.model.ProductRecord;

import java.util.List;

public interface ProductRepository {

    // All products that are made from a tray, that contains a part of the animal
    List<ProductRecord> findByAnimalRegNo(String regNo);

}
