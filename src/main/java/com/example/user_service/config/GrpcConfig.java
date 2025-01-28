package com.example.user_service.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pets.PetServiceGrpc;

@Configuration
public class GrpcConfig {

    @Bean
    public ManagedChannel petServiceChannel() {
        // Ajuste "localhost" e a porta 50051 se seu Pet Service estiver em outro host/porta
        return ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
    }

    @Bean
    public PetServiceGrpc.PetServiceBlockingStub petServiceStub(ManagedChannel channel) {
        return PetServiceGrpc.newBlockingStub(channel);
    }
}
