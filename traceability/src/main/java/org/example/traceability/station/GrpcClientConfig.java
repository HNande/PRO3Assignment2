package org.example.traceability.station;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.slaughterhouse.grpc.CuttingServiceGrpc;
import org.example.slaughterhouse.grpc.PackingServiceGrpc;
import org.example.slaughterhouse.grpc.RegistrationServiceGrpc;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// gRPC channels to the three stations. They connect lazily,
// so the traceability service starts fine even if a station isn't running.
@Configuration
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel registrationChannel(@Value("${stations.registration.host}") String host,
                                              @Value("${stations.registration.port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel cuttingChannel(@Value("${stations.cutting.host}") String host,
                                         @Value("${stations.cutting.port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel packingChannel(@Value("${stations.packing.host}") String host,
                                         @Value("${stations.packing.port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean
    public RegistrationServiceGrpc.RegistrationServiceBlockingStub registrationStub(
            @Qualifier("registrationChannel") ManagedChannel channel) {
        return RegistrationServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public CuttingServiceGrpc.CuttingServiceBlockingStub cuttingStub(
            @Qualifier("cuttingChannel") ManagedChannel channel) {
        return CuttingServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public PackingServiceGrpc.PackingServiceBlockingStub packingStub(
            @Qualifier("packingChannel") ManagedChannel channel) {
        return PackingServiceGrpc.newBlockingStub(channel);
    }
}
