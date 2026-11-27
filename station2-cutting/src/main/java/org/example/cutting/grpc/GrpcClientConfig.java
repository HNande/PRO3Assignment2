package org.example.cutting.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.slaughterhouse.grpc.PackingServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// gRPC client towards station 3. The channel connects lazily,
// so station 2 starts fine even if station 3 isn't running.
@Configuration
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel packingChannel(@Value("${stations.packing.host}") String host,
                                         @Value("${stations.packing.port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean
    public PackingServiceGrpc.PackingServiceBlockingStub packingStub(ManagedChannel packingChannel) {
        return PackingServiceGrpc.newBlockingStub(packingChannel);
    }
}
