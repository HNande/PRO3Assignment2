package org.example.cutting.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.example.cutting.model.PartRecord;
import org.example.cutting.model.TrayRecord;
import org.example.cutting.service.CuttingService;
import org.example.cutting.service.NotFoundException;
import org.example.slaughterhouse.grpc.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Starts the gRPC server in memory and calls it through a real client stub,
// the same way Postman would, just without the network.
class GrpcCuttingServerTest {

    private CuttingService cuttingService;
    private Server server;
    private ManagedChannel channel;
    private CuttingServiceGrpc.CuttingServiceBlockingStub client;

    @BeforeEach
    void setUp() throws Exception {
        cuttingService = mock(CuttingService.class);

        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name)
                .addService(new GrpcCuttingServer(cuttingService))
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(name).build();
        client = CuttingServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void receiveAnimal() {
        Ack ack = client.receiveAnimal(Animal.newBuilder().setRegNo("A-1").setSpecies("Pig").build());

        assertTrue(ack.getAccepted());
        verify(cuttingService).receiveAnimal(any());
    }

    @Test
    void cutPart() {
        when(cuttingService.cutPart("A-1", "HAM", 12.5)).thenReturn(new PartRecord(9, "HAM", 12.5, "A-1", 5));

        Part part = client.cutPart(CutPartRequest.newBuilder()
                .setAnimalRegNo("A-1").setPartType("HAM").setWeightKg(12.5).build());

        assertEquals(9, part.getId());
        assertEquals(5, part.getTrayId());
    }

    @Test
    void cutPartOfUnknownAnimalGivesNotFound() {
        when(cuttingService.cutPart(any(), any(), anyDouble())).thenThrow(new NotFoundException("nope"));

        StatusRuntimeException e = assertThrows(StatusRuntimeException.class,
                () -> client.cutPart(CutPartRequest.newBuilder().setAnimalRegNo("X").setPartType("HAM").setWeightKg(1).build()));
        assertEquals(Status.Code.NOT_FOUND, e.getStatus().getCode());
    }

    @Test
    void badInputGivesInvalidArgument() {
        when(cuttingService.cutPart(any(), any(), anyDouble())).thenThrow(new IllegalArgumentException("bad"));

        StatusRuntimeException e = assertThrows(StatusRuntimeException.class,
                () -> client.cutPart(CutPartRequest.newBuilder().setAnimalRegNo("A-1").build()));
        assertEquals(Status.Code.INVALID_ARGUMENT, e.getStatus().getCode());
    }

    @Test
    void closeTrayReturnsTrayWithParts() {
        when(cuttingService.closeTray(1)).thenReturn(new TrayRecord(1, "HAM", 50, 23.9, true));
        when(cuttingService.getPartsInTray(1)).thenReturn(List.of(
                new PartRecord(1, "HAM", 12.1, "A-1001", 1),
                new PartRecord(4, "HAM", 11.8, "A-1002", 1)));

        Tray tray = client.closeTray(TrayIdRequest.newBuilder().setTrayId(1).build());

        assertTrue(tray.getClosed());
        assertEquals(2, tray.getPartsCount());
        assertEquals("A-1002", tray.getParts(1).getAnimalRegNo());
    }

    @Test
    void traceabilityLookups() {
        when(cuttingService.getTrayIdsForAnimal("A-1003")).thenReturn(List.of(3, 4));
        when(cuttingService.getAnimalRegNosInTrays(List.of(1))).thenReturn(List.of("A-1001", "A-1002"));

        assertEquals(List.of(3, 4), client.getTraysForAnimal(RegNoRequest.newBuilder().setRegNo("A-1003").build()).getTrayIdsList());
        assertEquals(List.of("A-1001", "A-1002"), client.getAnimalsInTrays(TrayIdList.newBuilder().addTrayIds(1).build()).getRegNosList());
    }
}
