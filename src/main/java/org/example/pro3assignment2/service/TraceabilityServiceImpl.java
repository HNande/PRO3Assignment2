package org.example.pro3assignment2.service;

import org.example.pro3assignment2.model.AnimalRecord;
import org.example.pro3assignment2.model.ProductRecord;
import org.example.pro3assignment2.repository.AnimalRepository;
import org.example.pro3assignment2.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraceabilityServiceImpl implements TraceabilityService {

    private AnimalRepository animalRepository;
    private ProductRepository productRepository;

    // Spring gives us whatever implements the interfaces (dependency inversion)
    @Autowired
    public TraceabilityServiceImpl(AnimalRepository animalRepository, ProductRepository productRepository) {
        this.animalRepository = animalRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<AnimalRecord> getAnimalsInProduct(long productId){
        return animalRepository.findByProductId(productId);
    }

    @Override
    public List<ProductRecord> getProductsForAnimal(String regNo){
        return productRepository.findByAnimalRegNo(regNo.trim().toUpperCase());
    }
}
