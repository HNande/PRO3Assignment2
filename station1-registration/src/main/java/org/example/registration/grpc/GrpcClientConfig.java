package org.example.registration.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.slaughterhouse.grpc.CuttingServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// gRPC client towards station 2. The channel connects lazily,
// so station 1 starts fine even if station 2 isn't running.
@Configuration
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel cuttingChannel(@Value("${stations.cutting.host}") String host,
                                         @Value("${stations.cutting.port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean
    public CuttingServiceGrpc.CuttingServiceBlockingStub cuttingStub(ManagedChannel cuttingChannel) {
        return CuttingServiceGrpc.newBlockingStub(cuttingChannel);
    }
}
