package org.example.registration.controller;

import org.example.registration.model.AnimalRecord;
import org.example.registration.service.AnimalAlreadyRegisteredException;
import org.example.registration.service.AnimalRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnimalRegistrationController.class)
class AnimalRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalRegistrationService animalRegistrationService;

    private final AnimalRecord animal1 = new AnimalRecord("A-1001", "Pig", 112.5, LocalDate.of(2026, 10, 1), "Sundsvik Farm");
    private final AnimalRecord animal2 = new AnimalRecord("A-1002", "Pig", 108.0, LocalDate.of(2026, 10, 1), "Sundsvik Farm");
    private final AnimalRecord animal3 = new AnimalRecord("A-1003", "Pig", 120.3, LocalDate.of(2026, 10, 2), "Gävle Farm");

    @Test
    void registerAnimalReturns201Created() throws Exception {
        when(animalRegistrationService.registerAnimal(any())).thenReturn(animal1);

        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "regNo": "A-1001",
                                  "species": "Pig",
                                  "weightKg": 112.5,
                                  "arrivalDate": "2026-10-01",
                                  "origin": "Sundsvik Farm"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.regNo").value("A-1001"))
                .andExpect(jsonPath("$.species").value("Pig"))
                .andExpect(jsonPath("$.weightKg").value(112.5))
                .andExpect(jsonPath("$.origin").value("Sundsvik Farm"));
    }

    @Test
    void registeringExistingAnimalReturns409Conflict() throws Exception {
        when(animalRegistrationService.registerAnimal(any()))
                .thenThrow(new AnimalAlreadyRegisteredException("A-1001"));

        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "regNo": "A-1001",
                                  "species": "Pig",
                                  "weightKg": 112.5,
                                  "arrivalDate": "2026-10-01",
                                  "origin": "Sundsvik Farm"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void getAnimalByRegNoReturns200WhenFound() throws Exception {
        when(animalRegistrationService.getAnimalByRegistrationNumber("A-1001"))
                .thenReturn(Optional.of(animal1));

        mockMvc.perform(get("/api/animals/A-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regNo").value("A-1001"))
                .andExpect(jsonPath("$.species").value("Pig"))
                .andExpect(jsonPath("$.weightKg").value(112.5))
                .andExpect(jsonPath("$.arrivalDate").value("2026-10-01"))
                .andExpect(jsonPath("$.origin").value("Sundsvik Farm"));
    }

    @Test
    void getAnimalByRegNoReturns404WhenNotFound() throws Exception {
        when(animalRegistrationService.getAnimalByRegistrationNumber("NOPE"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/animals/NOPE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAnimalsByDateReturnsMatchingAnimals() throws Exception {
        when(animalRegistrationService.getAnimalsByArrivalDate(LocalDate.of(2026, 10, 1)))
                .thenReturn(List.of(animal1, animal2));

        mockMvc.perform(get("/api/animals/by-date").param("date", "2026-10-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].regNo").value("A-1001"))
                .andExpect(jsonPath("$[1].regNo").value("A-1002"));
    }

    @Test
    void getAnimalsByDateReturnsEmptyListWhenNoMatch() throws Exception {
        when(animalRegistrationService.getAnimalsByArrivalDate(LocalDate.of(2020, 1, 1)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/animals/by-date").param("date", "2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAnimalsByOriginReturnsMatchingAnimals() throws Exception {
        when(animalRegistrationService.getAnimalsByOrigin("Sundsvik Farm"))
                .thenReturn(List.of(animal1, animal2));

        mockMvc.perform(get("/api/animals/by-origin").param("origin", "Sundsvik Farm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].origin").value("Sundsvik Farm"))
                .andExpect(jsonPath("$[1].origin").value("Sundsvik Farm"));
    }

    @Test
    void getAnimalsByOriginReturnsEmptyListWhenNoMatch() throws Exception {
        when(animalRegistrationService.getAnimalsByOrigin("Unknown Farm"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/animals/by-origin").param("origin", "Unknown Farm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
