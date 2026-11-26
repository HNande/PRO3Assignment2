package org.example.traceability.station;

import io.grpc.StatusRuntimeException;
import org.example.slaughterhouse.grpc.*;
import org.example.traceability.model.AnimalRecord;
import org.example.traceability.model.ProductRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

// gRPC clients for the three stations
@Component
public class GrpcStationClients implements RegistrationClient, CuttingClient, PackingClient {

    private final RegistrationServiceGrpc.RegistrationServiceBlockingStub registrationStub;
    private final CuttingServiceGrpc.CuttingServiceBlockingStub cuttingStub;
    private final PackingServiceGrpc.PackingServiceBlockingStub packingStub;
    private final int timeoutSeconds;

    public GrpcStationClients(RegistrationServiceGrpc.RegistrationServiceBlockingStub registrationStub,
                              CuttingServiceGrpc.CuttingServiceBlockingStub cuttingStub,
                              PackingServiceGrpc.PackingServiceBlockingStub packingStub,
                              @Value("${stations.timeout-seconds:3}") int timeoutSeconds) {
        this.registrationStub = registrationStub;
        this.cuttingStub = cuttingStub;
        this.packingStub = packingStub;
        this.timeoutSeconds = timeoutSeconds;
    }

    // --- station 1 ---

    @Override
    public List<AnimalRecord> getAnimals(List<String> regNos) {
        AnimalList animals = call("Station 1 (registration)", () -> registrationStub
                .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                .getAnimals(RegNoList.newBuilder().addAllRegNos(regNos).build()));

        return animals.getAnimalsList().stream()
                .map(a -> new AnimalRecord(a.getRegNo(), a.getSpecies(), a.getWeightKg(),
                        LocalDate.parse(a.getArrivalDate()), a.getOrigin()))
                .toList();
    }

    // --- station 2 ---

    @Override
    public List<Integer> getTrayIdsForAnimal(String regNo) {
        return call("Station 2 (cutting)", () -> cuttingStub
                .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                .getTraysForAnimal(RegNoRequest.newBuilder().setRegNo(regNo).build()))
                .getTrayIdsList();
    }

    @Override
    public List<String> getAnimalRegNosInTrays(List<Integer> trayIds) {
        return call("Station 2 (cutting)", () -> cuttingStub
                .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                .getAnimalsInTrays(TrayIdList.newBuilder().addAllTrayIds(trayIds).build()))
                .getRegNosList();
    }

    // --- station 3 ---

    @Override
    public List<Integer> getTrayIdsInProduct(int productId) {
        return call("Station 3 (packing)", () -> packingStub
                .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                .getTraysInProduct(ProductIdRequest.newBuilder().setProductId(productId).build()))
                .getTrayIdsList();
    }

    @Override
    public List<ProductRecord> getProductsForTrays(List<Integer> trayIds) {
        ProductList products = call("Station 3 (packing)", () -> packingStub
                .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                .getProductsForTrays(TrayIdList.newBuilder().addAllTrayIds(trayIds).build()));

        return products.getProductsList().stream()
                .map(p -> new ProductRecord(p.getId(), p.getType(), p.getTrayIdsList()))
                .toList();
    }

    private static <T> T call(String station, Supplier<T> grpcCall) {
        try {
            return grpcCall.get();
        } catch (StatusRuntimeException e) {
            throw new StationUnavailableException(station, e);
        }
    }
}
