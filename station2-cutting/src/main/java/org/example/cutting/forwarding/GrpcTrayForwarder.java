package org.example.cutting.forwarding;

import io.grpc.StatusRuntimeException;
import org.example.cutting.grpc.CuttingMapper;
import org.example.cutting.model.TrayRecord;
import org.example.cutting.repository.PartRepository;
import org.example.cutting.repository.TrayRepository;
import org.example.slaughterhouse.grpc.PackingServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

// Store-and-forward: trays and parts are always saved in station 2's own database first,
// closed trays are then sent to station 3. If the network is down we just try again later,
// so station 2 keeps cutting.
@Component
public class GrpcTrayForwarder implements TrayForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcTrayForwarder.class);

    private final TrayRepository trayRepository;
    private final PartRepository partRepository;
    private final PackingServiceGrpc.PackingServiceBlockingStub packingStub;
    private final int timeoutSeconds;

    public GrpcTrayForwarder(TrayRepository trayRepository,
                             PartRepository partRepository,
                             PackingServiceGrpc.PackingServiceBlockingStub packingStub,
                             @Value("${stations.timeout-seconds:3}") int timeoutSeconds) {
        this.trayRepository = trayRepository;
        this.partRepository = partRepository;
        this.packingStub = packingStub;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public synchronized void forward(int trayId) {
        Optional<TrayRecord> tray = trayRepository.findById(trayId);
        if (tray.isEmpty()) {
            return;
        }
        try {
            packingStub.withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .receiveTray(CuttingMapper.toProto(tray.get(), partRepository.findByTrayId(trayId)));
            trayRepository.markForwarded(trayId);
            LOG.info("Tray {} sent to station 3", trayId);
        } catch (StatusRuntimeException e) {
            LOG.warn("Could not send tray {} to station 3 ({}), will retry later",
                    trayId, e.getStatus().getCode());
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${stations.retry-delay-ms:10000}")
    public void forwardPending() {
        for (int trayId : trayRepository.findClosedNotForwardedIds()) {
            forward(trayId);
        }
    }
}
