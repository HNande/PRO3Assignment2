package org.example.pro3assignment2.repository;

import org.example.pro3assignment2.model.AnimalRecord;
import org.example.pro3assignment2.model.ProductRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Runs the real SQL against H2 filled with schema.sql + data.sql
@SpringBootTest
class RepositoryTest {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void hamPackageContainsTheTwoAnimalsFromTrayOne() {
        List<AnimalRecord> animals = animalRepository.findByProductId(1);
        assertEquals(List.of("A-1001", "A-1002"), animals.stream().map(AnimalRecord::regNo).toList());
    }

    @Test
    void halfAnimalContainsAllThreeAnimals() {
        assertEquals(3, animalRepository.findByProductId(3).size());
    }

    @Test
    void animalIsFoundInAllItsProducts() {
        List<ProductRecord> products = productRepository.findByAnimalRegNo("A-1003");
        assertEquals(List.of(3L, 4L), products.stream().map(ProductRecord::id).toList());
    }

    @Test
    void unknownAnimalHasNoProducts() {
        assertTrue(productRepository.findByAnimalRegNo("NOPE").isEmpty());
    }
}
