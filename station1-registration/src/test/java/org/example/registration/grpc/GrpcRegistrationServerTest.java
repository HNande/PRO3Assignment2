package org.example.registration.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.example.registration.model.AnimalRecord;
import org.example.registration.service.AnimalRegistrationService;
import org.example.slaughterhouse.grpc.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// Starts the gRPC server in memory and calls it through a real client stub.
class GrpcRegistrationServerTest {

    private static final AnimalRecord PIG = new AnimalRecord("A-1001", "Pig", 112.5, LocalDate.of(2026, 10, 1), "Sundsvik Farm");

    private Server server;
    private ManagedChannel channel;
    private RegistrationServiceGrpc.RegistrationServiceBlockingStub client;

    @BeforeEach
    void setUp() throws Exception {
        AnimalRegistrationService fakeService = new AnimalRegistrationService() {
            @Override
            public AnimalRecord registerAnimal(AnimalRecord animal) {
                return animal;
            }

            @Override
            public Optional<AnimalRecord> getAnimalByRegistrationNumber(String regNo) {
                return regNo.equals("A-1001") ? Optional.of(PIG) : Optional.empty();
            }

            @Override
            public List<AnimalRecord> getAnimalsByRegistrationNumbers(List<String> regNos) {
                return regNos.contains("A-1001") ? List.of(PIG) : List.of();
            }

            @Override
            public List<AnimalRecord> getAnimalsByArrivalDate(LocalDate date) {
                return List.of();
            }

            @Override
            public List<AnimalRecord> getAnimalsByOrigin(String origin) {
                return List.of();
            }
        };

        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name)
                .addService(new GrpcRegistrationServer(fakeService))
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(name).build();
        client = RegistrationServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void getAnimal() {
        Animal animal = client.getAnimal(RegNoRequest.newBuilder().setRegNo("A-1001").build());

        assertEquals("Pig", animal.getSpecies());
        assertEquals(112.5, animal.getWeightKg());
        assertEquals("2026-10-01", animal.getArrivalDate());
        assertEquals("Sundsvik Farm", animal.getOrigin());
    }

    @Test
    void unknownAnimalGivesNotFound() {
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class,
                () -> client.getAnimal(RegNoRequest.newBuilder().setRegNo("NOPE").build()));
        assertEquals(Status.Code.NOT_FOUND, e.getStatus().getCode());
    }

    @Test
    void getAnimals() {
        AnimalList animals = client.getAnimals(RegNoList.newBuilder().addRegNos("A-1001").addRegNos("NOPE").build());

        assertEquals(1, animals.getAnimalsCount());
        assertEquals("A-1001", animals.getAnimals(0).getRegNo());
    }
}
