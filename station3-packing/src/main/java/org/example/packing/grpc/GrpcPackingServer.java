package org.example.packing.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.packing.model.PartRecord;
import org.example.packing.model.ProductRecord;
import org.example.packing.service.NotFoundException;
import org.example.packing.service.PackingService;
import org.example.slaughterhouse.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;
import java.util.List;
import java.util.function.Supplier;

@GrpcService
public class GrpcPackingServer extends PackingServiceGrpc.PackingServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcPackingServer.class);

    private final PackingService packingService;

    public GrpcPackingServer(PackingService packingService) {
        this.packingService = packingService;
    }

    @Override
    public void receiveTray(Tray request, StreamObserver<Ack> responseObserver) {
        respond(responseObserver, () -> {
            List<PartRecord> parts = request.getPartsList().stream()
                    .map(p -> new PartRecord(p.getId(), p.getPartType(), p.getWeightKg(), request.getId()))
                    .toList();
            packingService.receiveTray(request.getId(), request.getPartType(), parts);
            LOG.info("Received tray {} ({} x {}) from station 2", request.getId(), parts.size(), request.getPartType());
            return Ack.newBuilder().setAccepted(true).build();
        });
    }

    @Override
    public void packPackage(PackPackageRequest request, StreamObserver<Product> responseObserver) {
        respond(responseObserver, () -> {
            ProductRecord product = packingService.packPackage(request.getPartType(), request.getPartCount());
            LOG.info("Packed product {} ({}) from trays {}", product.id(), product.type(), product.trayIds());
            return toProto(product);
        });
    }

    @Override
    public void packHalfAnimal(PackHalfAnimalRequest request, StreamObserver<Product> responseObserver) {
        respond(responseObserver, () -> {
            ProductRecord product = packingService.packHalfAnimal();
            LOG.info("Packed product {} ({}) from trays {}", product.id(), product.type(), product.trayIds());
            return toProto(product);
        });
    }

    @Override
    public void getProduct(ProductIdRequest request, StreamObserver<Product> responseObserver) {
        respond(responseObserver, () -> toProto(packingService.getProduct(request.getProductId())));
    }

    @Override
    public void getTraysInProduct(ProductIdRequest request, StreamObserver<TrayIdList> responseObserver) {
        respond(responseObserver, () -> TrayIdList.newBuilder()
                .addAllTrayIds(packingService.getTrayIdsInProduct(request.getProductId()))
                .build());
    }

    @Override
    public void getProductsForTrays(TrayIdList request, StreamObserver<ProductList> responseObserver) {
        respond(responseObserver, () -> {
            ProductList.Builder response = ProductList.newBuilder();
            for (ProductRecord product : packingService.getProductsForTrays(request.getTrayIdsList())) {
                response.addProducts(toProto(product));
            }
            return response.build();
        });
    }

    private static Product toProto(ProductRecord product) {
        return Product.newBuilder()
                .setId(product.id())
                .setType(product.type())
                .addAllTrayIds(product.trayIds())
                .addAllPartIds(product.partIds())
                .build();
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
        } catch (IllegalStateException e) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
