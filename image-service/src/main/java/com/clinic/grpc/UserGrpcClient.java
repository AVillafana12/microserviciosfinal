package com.clinic.grpc;

import com.clinic.user.proto.GetUserByKeycloakIdRequest;
import com.clinic.user.proto.UserResponse;
import com.clinic.user.proto.UserServiceGrpcGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserServiceGrpcGrpc.UserServiceGrpcBlockingStub userServiceStub;

    public Integer getUserIdByKeycloakId(String keycloakId) {
        try {
            GetUserByKeycloakIdRequest request = GetUserByKeycloakIdRequest.newBuilder()
                    .setKeycloakId(keycloakId)
                    .build();
            
            UserResponse response = userServiceStub.getUserByKeycloakId(request);
            
            if (response.getId() == null || response.getId().isEmpty()) {
                throw new RuntimeException("User not found for keycloak ID: " + keycloakId);
            }
            
            return Integer.valueOf(response.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error calling user service via gRPC: " + e.getMessage(), e);
        }
    }
}
