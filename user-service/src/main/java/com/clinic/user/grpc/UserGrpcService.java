package com.clinic.user.grpc;

import com.clinic.user.model.User;
import com.clinic.user.proto.*;
import com.clinic.user.service.UserService;
import net.devh.boot.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;

import java.util.List;

@GrpcService
public class UserGrpcService extends UserServiceGrpcGrpc.UserServiceGrpcImplBase {

    private final UserService userService;

    public UserGrpcService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void getUserById(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            Integer userId = Integer.valueOf(request.getId());
            User u = userService.obtenerPorId(userId);
            if (u == null) {
                responseObserver.onNext(UserResponse.newBuilder().setId("").build());
                responseObserver.onCompleted();
                return;
            }

            UserResponse resp = UserResponse.newBuilder()
                    .setId(String.valueOf(u.getId()))
                    .setNombre(u.getNombre())
                    .setApellido(u.getApellido())
                    .setCorreo(u.getCorreo())
                    .setTelefono(u.getTelefono() == null ? "" : u.getTelefono())
                    .setRole(u.getRole() == null ? "" : u.getRole().name())
                    .build();

            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (NumberFormatException e) {
            responseObserver.onNext(UserResponse.newBuilder().setId("").build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listUsers(Empty request, StreamObserver<UserListResponse> responseObserver) {
        List<User> list = userService.obtenerUsuarios();
        UserListResponse.Builder b = UserListResponse.newBuilder();
        for (User u : list) {
            b.addUsers(UserResponse.newBuilder()
                    .setId(String.valueOf(u.getId()))
                    .setNombre(u.getNombre())
                    .setApellido(u.getApellido())
                    .setCorreo(u.getCorreo())
                    .setTelefono(u.getTelefono() == null ? "" : u.getTelefono())
                    .setRole(u.getRole() == null ? "" : u.getRole().name())
                    .build());
        }
        responseObserver.onNext(b.build());
        responseObserver.onCompleted();
    }

}
