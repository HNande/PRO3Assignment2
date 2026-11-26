package org.example.registration.service;

import org.example.registration.model.AnimalRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnimalRegistrationService {

    // throws AnimalAlreadyRegisteredException if the registration number is taken
    AnimalRecord registerAnimal(AnimalRecord animal);

    Optional<AnimalRecord> getAnimalByRegistrationNumber(String regNo);

    List<AnimalRecord> getAnimalsByRegistrationNumbers(List<String> regNos);

    List<AnimalRecord> getAnimalsByArrivalDate(LocalDate date);

    List<AnimalRecord> getAnimalsByOrigin(String origin);

}
