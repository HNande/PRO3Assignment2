package org.example.pro3assignment2.service;

import org.example.pro3assignment2.model.AnimalRecord;
import org.example.pro3assignment2.repository.AnimalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AnimalRegistrationServiceImpl implements AnimalRegistrationService {

    private AnimalRepository animalRepository;

    @Autowired
    public AnimalRegistrationServiceImpl(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }

    @Override
    public AnimalRecord registerAnimal(AnimalRecord animal) {
        animalRepository.save(animal);
        return animal;
    }

    @Override
    public Optional<AnimalRecord> getAnimalByRegistrationNumber(String regNo) {
        return animalRepository.findByRegistrationNumber(regNo.trim().toUpperCase());
    }

    @Override
    public List<AnimalRecord> getAnimalsByArrivalDate(LocalDate date) {
        return animalRepository.findByArrivalDate(date);
    }

    @Override
    public List<AnimalRecord> getAnimalsByOrigin(String origin) {
        return animalRepository.findByOrigin(origin);
    }
}
