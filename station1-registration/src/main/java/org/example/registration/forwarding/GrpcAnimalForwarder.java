package org.example.registration.forwarding;

import io.grpc.StatusRuntimeException;
import org.example.registration.grpc.AnimalMapper;
import org.example.registration.model.AnimalRecord;
import org.example.registration.repository.AnimalRepository;
import org.example.slaughterhouse.grpc.CuttingServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

// Store-and-forward: animals are always saved in station 1's own database first,
// then sent to station 2. If the network is down we just try again later,
// so station 1 keeps working.
@Component
public class GrpcAnimalForwarder implements AnimalForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcAnimalForwarder.class);

    private final AnimalRepository animalRepository;
    private final CuttingServiceGrpc.CuttingServiceBlockingStub cuttingStub;
    private final int timeoutSeconds;

    public GrpcAnimalForwarder(AnimalRepository animalRepository,
                               CuttingServiceGrpc.CuttingServiceBlockingStub cuttingStub,
                               @Value("${stations.timeout-seconds:3}") int timeoutSeconds) {
        this.animalRepository = animalRepository;
        this.cuttingStub = cuttingStub;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public synchronized void forward(AnimalRecord animal) {
        try {
            cuttingStub.withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                    .receiveAnimal(AnimalMapper.toProto(animal));
            animalRepository.markForwarded(animal.regNo());
            LOG.info("Animal {} sent to station 2", animal.regNo());
        } catch (StatusRuntimeException e) {
            LOG.warn("Could not send animal {} to station 2 ({}), will retry later",
                    animal.regNo(), e.getStatus().getCode());
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${stations.retry-delay-ms:10000}")
    public void forwardPending() {
        for (AnimalRecord animal : animalRepository.findNotForwarded()) {
            forward(animal);
        }
    }
}
