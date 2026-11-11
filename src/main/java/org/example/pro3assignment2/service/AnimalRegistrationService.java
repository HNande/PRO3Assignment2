package org.example.pro3assignment2.service;

import org.example.pro3assignment2.model.AnimalRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnimalRegistrationService {

    AnimalRecord registerAnimal(AnimalRecord animal);

    Optional<AnimalRecord> getAnimalByRegistrationNumber(String regNo);

    List<AnimalRecord> getAnimalsByArrivalDate(LocalDate date);

    List<AnimalRecord> getAnimalsByOrigin(String origin);

}
