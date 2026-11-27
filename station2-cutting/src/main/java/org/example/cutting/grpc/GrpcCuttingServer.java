package org.example.cutting.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.cutting.model.AnimalRecord;
import org.example.cutting.model.PartRecord;
import org.example.cutting.model.TrayRecord;
import org.example.cutting.service.CuttingService;
import org.example.cutting.service.NotFoundException;
import org.example.slaughterhouse.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;
import java.util.function.Supplier;

@GrpcService
public class GrpcCuttingServer extends CuttingServiceGrpc.CuttingServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcCuttingServer.class);

    private final CuttingService cuttingService;

    public GrpcCuttingServer(CuttingService cuttingService) {
        this.cuttingService = cuttingService;
    }

    @Override
    public void receiveAnimal(Animal request, StreamObserver<Ack> responseObserver) {
        respond(responseObserver, () -> {
            cuttingService.receiveAnimal(new AnimalRecord(request.getRegNo(), request.getSpecies()));
            LOG.info("Received animal {} from station 1", request.getRegNo());
            return Ack.newBuilder().setAccepted(true).build();
        });
    }

    @Override
    public void cutPart(CutPartRequest request, StreamObserver<Part> responseObserver) {
        respond(responseObserver, () -> {
            PartRecord part = cuttingService.cutPart(request.getAnimalRegNo(), request.getPartType(), request.getWeightKg());
            LOG.info("Part {} ({}) of animal {} put into tray {}", part.id(), part.partType(), part.animalRegNo(), part.trayId());
            return CuttingMapper.toProto(part);
        });
    }

    @Override
    public void closeTray(TrayIdRequest request, StreamObserver<Tray> responseObserver) {
        respond(responseObserver, () -> {
            TrayRecord tray = cuttingService.closeTray(request.getTrayId());
            return CuttingMapper.toProto(tray, cuttingService.getPartsInTray(tray.id()));
        });
    }

    @Override
    public void getTray(TrayIdRequest request, StreamObserver<Tray> responseObserver) {
        respond(responseObserver, () -> {
            TrayRecord tray = cuttingService.getTray(request.getTrayId());
            return CuttingMapper.toProto(tray, cuttingService.getPartsInTray(tray.id()));
        });
    }

    @Override
    public void getTraysForAnimal(RegNoRequest request, StreamObserver<TrayIdList> responseObserver) {
        respond(responseObserver, () -> TrayIdList.newBuilder()
                .addAllTrayIds(cuttingService.getTrayIdsForAnimal(request.getRegNo()))
                .build());
    }

    @Override
    public void getAnimalsInTrays(TrayIdList request, StreamObserver<RegNoList> responseObserver) {
        respond(responseObserver, () -> RegNoList.newBuilder()
                .addAllRegNos(cuttingService.getAnimalRegNosInTrays(request.getTrayIdsList()))
                .build());
    }

    // sends the result, or turns our exceptions into proper gRPC status codes
    private static <T> void respond(StreamObserver<T> responseObserver, Supplier<T> action) {
        try {
            responseObserver.onNext(action.get());
            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
