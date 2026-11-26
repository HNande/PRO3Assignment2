package org.example.registration.forwarding;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.registration.model.AnimalRecord;
import org.example.registration.repository.AnimalRepository;
import org.example.slaughterhouse.grpc.Ack;
import org.example.slaughterhouse.grpc.Animal;
import org.example.slaughterhouse.grpc.CuttingServiceGrpc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

// A fake station 2 runs in memory. We check that animals reach it,
// and that nothing is lost while it is "down".
class GrpcAnimalForwarderTest {

    private final List<Animal> receivedByStation2 = new ArrayList<>();
    private final Map<String, AnimalRecord> notForwarded = new LinkedHashMap<>();

    private String serverName;
    private Server station2;
    private ManagedChannel channel;
    private GrpcAnimalForwarder forwarder;

    @BeforeEach
    void setUp() throws Exception {
        serverName = InProcessServerBuilder.generateName();
        startStation2();
        channel = InProcessChannelBuilder.forName(serverName).build();

        AnimalRepository fakeRepository = new AnimalRepository() {
            @Override
            public Optional<AnimalRecord> findByRegistrationNumber(String regNo) {
                return Optional.empty();
            }

            @Override
            public List<AnimalRecord> findByRegistrationNumbers(List<String> regNos) {
                return List.of();
            }

            @Override
            public List<AnimalRecord> findByArrivalDate(LocalDate date) {
                return List.of();
            }

            @Override
            public List<AnimalRecord> findByOrigin(String origin) {
                return List.of();
            }

            @Override
            public void save(AnimalRecord animal) {
                notForwarded.put(animal.regNo(), animal);
            }

            @Override
            public List<AnimalRecord> findNotForwarded() {
                return new ArrayList<>(notForwarded.values());
            }

            @Override
            public void markForwarded(String regNo) {
                notForwarded.remove(regNo);
            }
        };

        forwarder = new GrpcAnimalForwarder(fakeRepository, CuttingServiceGrpc.newBlockingStub(channel), 1);
    }

    private void startStation2() throws Exception {
        station2 = InProcessServerBuilder.forName(serverName)
                .addService(new CuttingServiceGrpc.CuttingServiceImplBase() {
                    @Override
                    public void receiveAnimal(Animal request, StreamObserver<Ack> responseObserver) {
                        receivedByStation2.add(request);
                        responseObserver.onNext(Ack.newBuilder().setAccepted(true).build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        station2.shutdownNow();
    }

    private AnimalRecord register(String regNo) {
        AnimalRecord animal = new AnimalRecord(regNo, "Pig", 100.0, LocalDate.of(2026, 11, 26), "Test Farm");
        notForwarded.put(regNo, animal);
        return animal;
    }

    @Test
    void animalIsSentToStation2AndMarkedForwarded() {
        forwarder.forward(register("A-1"));

        assertEquals(1, receivedByStation2.size());
        assertEquals("A-1", receivedByStation2.get(0).getRegNo());
        assertTrue(notForwarded.isEmpty());
    }

    @Test
    void animalIsKeptAndResentWhenStation2IsDown() throws Exception {
        station2.shutdownNow().awaitTermination();

        forwarder.forward(register("A-1")); // must not throw
        assertTrue(receivedByStation2.isEmpty());
        assertTrue(notForwarded.containsKey("A-1"));

        // station 2 comes back, the scheduled retry sends it
        startStation2();
        channel.resetConnectBackoff(); // don't wait for gRPC's reconnect backoff in a test
        forwarder.forwardPending();

        assertEquals(1, receivedByStation2.size());
        assertTrue(notForwarded.isEmpty());
    }
}
