package org.example.registration.controller;

import org.example.registration.model.AnimalRecord;
import org.example.registration.service.AnimalAlreadyRegisteredException;
import org.example.registration.service.AnimalRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/animals")
public class AnimalRegistrationController {

    private final AnimalRegistrationService animalRegistrationService;

    @Autowired
    public AnimalRegistrationController(AnimalRegistrationService animalRegistrationService) {
        this.animalRegistrationService = animalRegistrationService;
    }

    @PostMapping
    public ResponseEntity<AnimalRecord> registerAnimal(@RequestBody AnimalRecord animal) {
        AnimalRecord registered = animalRegistrationService.registerAnimal(animal);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    @ExceptionHandler(AnimalAlreadyRegisteredException.class)
    public ResponseEntity<String> handleDuplicate(AnimalAlreadyRegisteredException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @GetMapping("/{regNo}")
    public ResponseEntity<AnimalRecord> getAnimal(@PathVariable String regNo) {
        return animalRegistrationService.getAnimalByRegistrationNumber(regNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<AnimalRecord>> getAnimalsByDate(@RequestParam LocalDate date) {
        return ResponseEntity.ok(animalRegistrationService.getAnimalsByArrivalDate(date));
    }

    @GetMapping("/by-origin")
    public ResponseEntity<List<AnimalRecord>> getAnimalsByOrigin(@RequestParam String origin) {
        return ResponseEntity.ok(animalRegistrationService.getAnimalsByOrigin(origin));
    }
}
