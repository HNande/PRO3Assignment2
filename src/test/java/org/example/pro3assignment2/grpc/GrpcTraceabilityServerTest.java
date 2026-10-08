package org.example.pro3assignment2.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.example.pro3assignment2.generated.*;
import org.example.pro3assignment2.model.AnimalRecord;
import org.example.pro3assignment2.model.ProductRecord;
import org.example.pro3assignment2.service.TraceabilityService;
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
            public List<AnimalRecord> getAnimalsInProduct(long productId) {
                return List.of(new AnimalRecord("A-1001", "Pig", 112.5, LocalDate.of(2026, 10, 1)));
            }

            @Override
            public List<ProductRecord> getProductsForAnimal(String regNo) {
                return List.of(new ProductRecord(1, "PACKAGE"), new ProductRecord(3, "HALF_ANIMAL"));
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
    }
}
