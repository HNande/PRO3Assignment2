package org.example.traceability.station;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.slaughterhouse.grpc.*;
import org.example.traceability.model.AnimalRecord;
import org.example.traceability.model.ProductRecord;
import org.example.traceability.service.TraceabilityService;
import org.example.traceability.service.TraceabilityServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// The three stations run as fake gRPC servers in memory, holding the same data as the
// stations' data.sql. The real gRPC clients and the real TraceabilityServiceImpl
// follow the chain across them.
class GrpcStationClientsTest {

    private Server stations;
    private ManagedChannel channel;
    private TraceabilityService traceability;

    @BeforeEach
    void setUp() throws Exception {
        String name = InProcessServerBuilder.generateName();
        stations = InProcessServerBuilder.forName(name)
                .addService(new FakeStation1())
                .addService(new FakeStation2())
                .addService(new FakeStation3())
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(name).build();

        GrpcStationClients clients = new GrpcStationClients(
                RegistrationServiceGrpc.newBlockingStub(channel),
                CuttingServiceGrpc.newBlockingStub(channel),
                PackingServiceGrpc.newBlockingStub(channel),
                1);
        traceability = new TraceabilityServiceImpl(clients, clients, clients);
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        stations.shutdownNow();
    }

    @Test
    void hamPackageContainsTheTwoAnimalsFromTrayOne() {
        List<AnimalRecord> animals = traceability.getAnimalsInProduct(1);

        assertEquals(List.of("A-1001", "A-1002"), animals.stream().map(AnimalRecord::regNo).toList());
        assertEquals(LocalDate.of(2026, 10, 1), animals.get(0).arrivalDate());
        assertEquals("Sundsvik Farm", animals.get(0).origin());
    }

    @Test
    void halfAnimalContainsAllThreeAnimals() {
        assertEquals(3, traceability.getAnimalsInProduct(3).size());
    }

    @Test
    void recallFindsAllProductsOfAnAnimal() {
        List<ProductRecord> products = traceability.getProductsForAnimal("a-1003");

        assertEquals(List.of(3), products.stream().map(ProductRecord::id).toList());
        assertEquals(List.of(2, 3, 4), products.get(0).trayIds());
    }

    @Test
    void unknownAnimalHasNoProducts() {
        assertTrue(traceability.getProductsForAnimal("NOPE").isEmpty());
    }

    @Test
    void stationDownGivesStationUnavailable() throws Exception {
        stations.shutdownNow().awaitTermination();

        StationUnavailableException e = assertThrows(StationUnavailableException.class,
                () -> traceability.getProductsForAnimal("A-1001"));
        assertTrue(e.getMessage().contains("Station 2"));
    }

    // --- fake stations with the data from the stations' data.sql ---

    private static class FakeStation1 extends RegistrationServiceGrpc.RegistrationServiceImplBase {
        private static final List<Animal> ANIMALS = List.of(
                animal("A-1001", 112.5, "2026-10-01", "Sundsvik Farm"),
                animal("A-1002", 108.0, "2026-10-01", "Sundsvik Farm"),
                animal("A-1003", 120.3, "2026-10-02", "Gävle Farm"));

        private static Animal animal(String regNo, double weight, String date, String origin) {
            return Animal.newBuilder().setRegNo(regNo).setSpecies("Pig").setWeightKg(weight)
                    .setArrivalDate(date).setOrigin(origin).build();
        }

        @Override
        public void getAnimals(RegNoList request, StreamObserver<AnimalList> responseObserver) {
            responseObserver.onNext(AnimalList.newBuilder()
                    .addAllAnimals(ANIMALS.stream().filter(a -> request.getRegNosList().contains(a.getRegNo())).toList())
                    .build());
            responseObserver.onCompleted();
        }
    }

    private static class FakeStation2 extends CuttingServiceGrpc.CuttingServiceImplBase {
        // part -> (animal, tray)
        private static final List<String[]> PARTS = List.of(
                new String[]{"A-1001", "1"}, new String[]{"A-1001", "2"}, new String[]{"A-1001", "3"},
                new String[]{"A-1002", "1"}, new String[]{"A-1002", "2"}, new String[]{"A-1002", "3"},
                new String[]{"A-1003", "4"}, new String[]{"A-1003", "3"});

        @Override
        public void getTraysForAnimal(RegNoRequest request, StreamObserver<TrayIdList> responseObserver) {
            responseObserver.onNext(TrayIdList.newBuilder().addAllTrayIds(PARTS.stream()
                    .filter(p -> p[0].equals(request.getRegNo()))
                    .map(p -> Integer.parseInt(p[1])).distinct().sorted().toList()).build());
            responseObserver.onCompleted();
        }

        @Override
        public void getAnimalsInTrays(TrayIdList request, StreamObserver<RegNoList> responseObserver) {
            responseObserver.onNext(RegNoList.newBuilder().addAllRegNos(PARTS.stream()
                    .filter(p -> request.getTrayIdsList().contains(Integer.parseInt(p[1])))
                    .map(p -> p[0]).distinct().sorted().toList()).build());
            responseObserver.onCompleted();
        }
    }

    private static class FakeStation3 extends PackingServiceGrpc.PackingServiceImplBase {
        private static final List<Product> PRODUCTS = List.of(
                Product.newBuilder().setId(1).setType("PACKAGE").addTrayIds(1).build(),
                Product.newBuilder().setId(2).setType("PACKAGE").addTrayIds(2).build(),
                Product.newBuilder().setId(3).setType("HALF_ANIMAL").addAllTrayIds(List.of(2, 3, 4)).build());

        @Override
        public void getTraysInProduct(ProductIdRequest request, StreamObserver<TrayIdList> responseObserver) {
            responseObserver.onNext(TrayIdList.newBuilder().addAllTrayIds(PRODUCTS.stream()
                    .filter(p -> p.getId() == request.getProductId())
                    .flatMap(p -> p.getTrayIdsList().stream()).toList()).build());
            responseObserver.onCompleted();
        }

        @Override
        public void getProductsForTrays(TrayIdList request, StreamObserver<ProductList> responseObserver) {
            responseObserver.onNext(ProductList.newBuilder().addAllProducts(PRODUCTS.stream()
                    .filter(p -> p.getTrayIdsList().stream().anyMatch(request.getTrayIdsList()::contains))
                    .toList()).build());
            responseObserver.onCompleted();
        }
    }
}
