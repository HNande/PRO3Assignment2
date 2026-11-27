package org.example.cutting.repository;

import org.example.cutting.model.AnimalRecord;

public interface AnimalRepository {

    boolean exists(String regNo);

    void save(AnimalRecord animal);

}
