package org.example.traceability.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.slaughterhouse.grpc.*;
import org.example.traceability.model.AnimalRecord;
import org.example.traceability.model.ProductRecord;
import org.example.traceability.service.TraceabilityService;
import org.example.traceability.station.StationUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;
import java.util.List;

@GrpcService
public class GrpcTraceabilityServer extends TraceabilityServiceGrpc.TraceabilityServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcTraceabilityServer.class.getName());
    private final TraceabilityService traceabilityService;

    public GrpcTraceabilityServer(TraceabilityService traceabilityService){
        this.traceabilityService = traceabilityService;
    }

    @Override
    public void getAnimalsInProduct(GetAnimalsInProductRequest request, StreamObserver<AnimalsResponse> responseObserver) {
        try {
            List<AnimalRecord> animals = traceabilityService.getAnimalsInProduct(request.getProductId());

            AnimalsResponse.Builder response = AnimalsResponse.newBuilder();
            for (AnimalRecord animal : animals){
                response.addAnimals(Mapper.toProto(animal));
            }

            LOG.info("Product {} contains {} animal(s)", request.getProductId(), animals.size());
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (StationUnavailableException e) {
            responseObserver.onError(unavailable(e));
        }
    }

    @Override
    public void getProductsForAnimal(GetProductsForAnimalRequest request, StreamObserver<ProductsResponse> responseObserver) {
        try {
            List<ProductRecord> products = traceabilityService.getProductsForAnimal(request.getRegNo());

            ProductsResponse.Builder response = ProductsResponse.newBuilder();
            for (ProductRecord product : products){
                response.addProducts(Mapper.toProto(product));
            }

            LOG.info("Animal {} is in {} product(s)", request.getRegNo(), products.size());
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (StationUnavailableException e) {
            responseObserver.onError(unavailable(e));
        }
    }

    private static RuntimeException unavailable(StationUnavailableException e) {
        LOG.warn(e.getMessage());
        return Status.UNAVAILABLE.withDescription(e.getMessage()).withCause(e).asRuntimeException();
    }

    private static class Mapper {
        static Animal toProto(AnimalRecord animal){
            return Animal.newBuilder()
                    .setRegNo(animal.regNo())
                    .setSpecies(animal.species())
                    .setWeightKg(animal.weightKg())
                    .setArrivalDate(animal.arrivalDate().toString())
                    .setOrigin(animal.origin())
                    .build();
        }

        static Product toProto(ProductRecord product) {
            return Product.newBuilder()
                    .setId(product.id())
                    .setType(product.type())
                    .addAllTrayIds(product.trayIds())
                    .build();
        }
    }
}
