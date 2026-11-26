package org.example.traceability.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.example.slaughterhouse.grpc.*;
import org.example.traceability.model.AnimalRecord;
import org.example.traceability.model.ProductRecord;
import org.example.traceability.service.TraceabilityService;
import org.example.traceability.station.StationUnavailableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Starts the gRPC server in memory and calls it through a real client stub,
// the same way BloomRPC/Postman would, just without the network.
class GrpcTraceabilityServerTest {

    private Server server;
    private ManagedChannel channel;
    private TraceabilityServiceGrpc.TraceabilityServiceBlockingStub client;

    @BeforeEach
    void setUp() throws Exception {
        TraceabilityService fakeService = new TraceabilityService() {
            @Override
            public List<AnimalRecord> getAnimalsInProduct(int productId) {
                return List.of(new AnimalRecord("A-1001", "Pig", 112.5, LocalDate.of(2026, 10, 1), "Sundsvik Farm"));
            }

            @Override
            public List<ProductRecord> getProductsForAnimal(String regNo) {
                return List.of(new ProductRecord(1, "PACKAGE", List.of(1)), new ProductRecord(3, "HALF_ANIMAL", List.of(2, 3, 4)));
            }
        };

        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name)
                .addService(new GrpcTraceabilityServer(fakeService))
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(name).build();
        client = TraceabilityServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void getAnimalsInProduct() {
        AnimalsResponse response = client.getAnimalsInProduct(
                GetAnimalsInProductRequest.newBuilder().setProductId(1).build());

        assertEquals(1, response.getAnimalsCount());
        Animal animal = response.getAnimals(0);
        assertEquals("A-1001", animal.getRegNo());
        assertEquals("2026-10-01", animal.getArrivalDate());
    }

    @Test
    void getProductsForAnimal() {
        ProductsResponse response = client.getProductsForAnimal(
                GetProductsForAnimalRequest.newBuilder().setRegNo("A-1001").build());

        assertEquals(2, response.getProductsCount());
        assertEquals("HALF_ANIMAL", response.getProducts(1).getType());
        assertEquals(List.of(2, 3, 4), response.getProducts(1).getTrayIdsList());
    }

    @Test
    void stationDownGivesUnavailable() throws Exception {
        TraceabilityService failingService = new TraceabilityService() {
            @Override
            public List<AnimalRecord> getAnimalsInProduct(int productId) {
                throw new StationUnavailableException("Station 3 (packing)", null);
            }

            @Override
            public List<ProductRecord> getProductsForAnimal(String regNo) {
                throw new StationUnavailableException("Station 2 (cutting)", null);
            }
        };
        String name = InProcessServerBuilder.generateName();
        Server failingServer = InProcessServerBuilder.forName(name)
                .addService(new GrpcTraceabilityServer(failingService)).build().start();
        ManagedChannel failingChannel = InProcessChannelBuilder.forName(name).build();
        try {
            var failingClient = TraceabilityServiceGrpc.newBlockingStub(failingChannel);

            StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> failingClient
                    .getProductsForAnimal(GetProductsForAnimalRequest.newBuilder().setRegNo("A-1001").build()));
            assertEquals(Status.Code.UNAVAILABLE, e.getStatus().getCode());
            assertTrue(e.getStatus().getDescription().contains("Station 2"));
        } finally {
            failingChannel.shutdownNow();
            failingServer.shutdownNow();
        }
    }
}
