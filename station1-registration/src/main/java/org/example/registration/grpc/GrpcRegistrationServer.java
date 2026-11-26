package org.example.registration.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.registration.model.AnimalRecord;
import org.example.registration.service.AnimalRegistrationService;
import org.example.slaughterhouse.grpc.*;
import org.springframework.grpc.server.service.GrpcService;
import java.util.List;

// Lets the other subsystems (e.g. traceability) read animal details from station 1.
@GrpcService
public class GrpcRegistrationServer extends RegistrationServiceGrpc.RegistrationServiceImplBase {

    private final AnimalRegistrationService animalRegistrationService;

    public GrpcRegistrationServer(AnimalRegistrationService animalRegistrationService) {
        this.animalRegistrationService = animalRegistrationService;
    }

    @Override
    public void getAnimal(RegNoRequest request, StreamObserver<Animal> responseObserver) {
        animalRegistrationService.getAnimalByRegistrationNumber(request.getRegNo())
                .ifPresentOrElse(animal -> {
                    responseObserver.onNext(AnimalMapper.toProto(animal));
                    responseObserver.onCompleted();
                }, () -> responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Unknown animal " + request.getRegNo())
                        .asRuntimeException()));
    }

    @Override
    public void getAnimals(RegNoList request, StreamObserver<AnimalList> responseObserver) {
        List<AnimalRecord> animals = animalRegistrationService.getAnimalsByRegistrationNumbers(request.getRegNosList());

        AnimalList.Builder response = AnimalList.newBuilder();
        for (AnimalRecord animal : animals) {
            response.addAnimals(AnimalMapper.toProto(animal));
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
