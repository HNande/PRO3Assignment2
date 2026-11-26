package org.example.packing.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.example.packing.model.PartRecord;
import org.example.packing.model.ProductRecord;
import org.example.packing.service.NotFoundException;
import org.example.packing.service.PackingService;
import org.example.slaughterhouse.grpc.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Starts the gRPC server in memory and calls it through a real client stub,
// the same way Postman would, just without the network.
class GrpcPackingServerTest {

    private PackingService packingService;
    private Server server;
    private ManagedChannel channel;
    private PackingServiceGrpc.PackingServiceBlockingStub client;

    @BeforeEach
    void setUp() throws Exception {
        packingService = mock(PackingService.class);

        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name)
                .addService(new GrpcPackingServer(packingService))
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(name).build();
        client = PackingServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void receiveTrayPassesPartsOn() {
        Ack ack = client.receiveTray(Tray.newBuilder()
                .setId(5).setPartType("HAM")
                .addParts(Part.newBuilder().setId(9).setPartType("HAM").setWeightKg(12.0).setAnimalRegNo("A-1").setTrayId(5))
                .build());

        assertTrue(ack.getAccepted());
        verify(packingService).receiveTray(5, "HAM", List.of(new PartRecord(9, "HAM", 12.0, 5)));
    }

    @Test
    void packPackage() {
        when(packingService.packPackage("LOIN", 2))
                .thenReturn(new ProductRecord(4, ProductRecord.PACKAGE, List.of(3), List.of(6, 8)));

        Product product = client.packPackage(PackPackageRequest.newBuilder().setPartType("LOIN").setPartCount(2).build());

        assertEquals(4, product.getId());
        assertEquals("PACKAGE", product.getType());
        assertEquals(List.of(3), product.getTrayIdsList());
        assertEquals(List.of(6, 8), product.getPartIdsList());
    }

    @Test
    void notEnoughPartsGivesFailedPrecondition() {
        when(packingService.packHalfAnimal()).thenThrow(new IllegalStateException("No HAM"));

        StatusRuntimeException e = assertThrows(StatusRuntimeException.class,
                () -> client.packHalfAnimal(PackHalfAnimalRequest.getDefaultInstance()));
        assertEquals(Status.Code.FAILED_PRECONDITION, e.getStatus().getCode());
    }

    @Test
    void unknownProductGivesNotFound() {
        when(packingService.getProduct(999)).thenThrow(new NotFoundException("nope"));

        StatusRuntimeException e = assertThrows(StatusRuntimeException.class,
                () -> client.getProduct(ProductIdRequest.newBuilder().setProductId(999).build()));
        assertEquals(Status.Code.NOT_FOUND, e.getStatus().getCode());
    }

    @Test
    void traceabilityLookups() {
        when(packingService.getTrayIdsInProduct(3)).thenReturn(List.of(2, 3, 4));
        when(packingService.getProductsForTrays(List.of(4)))
                .thenReturn(List.of(new ProductRecord(3, ProductRecord.HALF_ANIMAL, List.of(2, 3, 4), List.of(3, 5, 7))));

        assertEquals(List.of(2, 3, 4),
                client.getTraysInProduct(ProductIdRequest.newBuilder().setProductId(3).build()).getTrayIdsList());
        ProductList products = client.getProductsForTrays(TrayIdList.newBuilder().addTrayIds(4).build());
        assertEquals(1, products.getProductsCount());
        assertEquals("HALF_ANIMAL", products.getProducts(0).getType());
    }
}
