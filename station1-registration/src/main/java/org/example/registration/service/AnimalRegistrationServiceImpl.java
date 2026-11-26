package org.example.registration.service;

import org.example.registration.forwarding.AnimalForwarder;
import org.example.registration.model.AnimalRecord;
import org.example.registration.repository.AnimalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AnimalRegistrationServiceImpl implements AnimalRegistrationService {

    private AnimalRepository animalRepository;
    private AnimalForwarder animalForwarder;

    @Autowired
    public AnimalRegistrationServiceImpl(AnimalRepository animalRepository, AnimalForwarder animalForwarder) {
        this.animalRepository = animalRepository;
        this.animalForwarder = animalForwarder;
    }

    @Override
    public AnimalRecord registerAnimal(AnimalRecord animal) {
        AnimalRecord cleaned = new AnimalRecord(
                normalize(animal.regNo()),
                animal.species(),
                animal.weightKg(),
                animal.arrivalDate(),
                animal.origin()
        );
        if (animalRepository.findByRegistrationNumber(cleaned.regNo()).isPresent()) {
            throw new AnimalAlreadyRegisteredException(cleaned.regNo());
        }

        // saved locally first, so registration works even if station 2 can't be reached
        animalRepository.save(cleaned);
        animalForwarder.forward(cleaned);
        return cleaned;
    }

    @Override
    public Optional<AnimalRecord> getAnimalByRegistrationNumber(String regNo) {
        return animalRepository.findByRegistrationNumber(normalize(regNo));
    }

    @Override
    public List<AnimalRecord> getAnimalsByRegistrationNumbers(List<String> regNos) {
        return animalRepository.findByRegistrationNumbers(regNos.stream().map(this::normalize).toList());
    }

    @Override
    public List<AnimalRecord> getAnimalsByArrivalDate(LocalDate date) {
        return animalRepository.findByArrivalDate(date);
    }

    @Override
    public List<AnimalRecord> getAnimalsByOrigin(String origin) {
        return animalRepository.findByOrigin(origin);
    }

    private String normalize(String regNo) {
        return regNo.trim().toUpperCase();
    }
}
