package org.example.traceability.service;

import org.example.traceability.model.AnimalRecord;
import org.example.traceability.model.ProductRecord;
import java.util.List;

public interface TraceabilityService {

    List<AnimalRecord> getAnimalsInProduct(int productId);

    List<ProductRecord> getProductsForAnimal(String regNo);

}
