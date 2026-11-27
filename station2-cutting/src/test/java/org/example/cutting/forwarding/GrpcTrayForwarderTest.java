package org.example.cutting.forwarding;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.cutting.model.PartRecord;
import org.example.cutting.model.TrayRecord;
import org.example.cutting.repository.PartRepository;
import org.example.cutting.repository.TrayRepository;
import org.example.slaughterhouse.grpc.Ack;
import org.example.slaughterhouse.grpc.PackingServiceGrpc;
import org.example.slaughterhouse.grpc.Tray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// A fake station 3 runs in memory. We check that trays reach it with their parts,
// and that nothing is lost while it is "down".
class GrpcTrayForwarderTest {

    private final List<Tray> receivedByStation3 = new ArrayList<>();

    private String serverName;
    private Server station3;
    private ManagedChannel channel;
    private TrayRepository trayRepository;
    private GrpcTrayForwarder forwarder;

    @BeforeEach
    void setUp() throws Exception {
        serverName = InProcessServerBuilder.generateName();
        startStation3();
        channel = InProcessChannelBuilder.forName(serverName).build();

        trayRepository = mock(TrayRepository.class);
        PartRepository partRepository = mock(PartRepository.class);
        when(trayRepository.findById(1)).thenReturn(Optional.of(new TrayRecord(1, "HAM", 50, 23.9, true)));
        when(partRepository.findByTrayId(1)).thenReturn(List.of(
                new PartRecord(1, "HAM", 12.1, "A-1001", 1),
                new PartRecord(4, "HAM", 11.8, "A-1002", 1)));

        forwarder = new GrpcTrayForwarder(trayRepository, partRepository, PackingServiceGrpc.newBlockingStub(channel), 1);
    }

    private void startStation3() throws Exception {
        station3 = InProcessServerBuilder.forName(serverName)
                .addService(new PackingServiceGrpc.PackingServiceImplBase() {
                    @Override
                    public void receiveTray(Tray request, StreamObserver<Ack> responseObserver) {
                        receivedByStation3.add(request);
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
        station3.shutdownNow();
    }

    @Test
    void trayIsSentWithItsPartsAndMarkedForwarded() {
        forwarder.forward(1);

        assertEquals(1, receivedByStation3.size());
        assertEquals(2, receivedByStation3.get(0).getPartsCount());
        verify(trayRepository).markForwarded(1);
    }

    @Test
    void trayIsKeptAndResentWhenStation3IsDown() throws Exception {
        station3.shutdownNow().awaitTermination();

        forwarder.forward(1); // must not throw
        assertTrue(receivedByStation3.isEmpty());
        verify(trayRepository, never()).markForwarded(anyInt());

        // station 3 comes back, the scheduled retry sends it
        startStation3();
        channel.resetConnectBackoff(); // don't wait for gRPC's reconnect backoff in a test
        when(trayRepository.findClosedNotForwardedIds()).thenReturn(List.of(1));
        forwarder.forwardPending();

        assertEquals(1, receivedByStation3.size());
        verify(trayRepository).markForwarded(1);
    }
}
