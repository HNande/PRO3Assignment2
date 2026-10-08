package org.example.pro3assignment2.service;

import org.example.pro3assignment2.model.AnimalRecord;
import org.example.pro3assignment2.model.ProductRecord;

import java.util.List;

public interface TraceabilityService {

    List<AnimalRecord> getAnimalsInProduct(long productId);

    List<ProductRecord> getProductsForAnimal(String regNo);

}
