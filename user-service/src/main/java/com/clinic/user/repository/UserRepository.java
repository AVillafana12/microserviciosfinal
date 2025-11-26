package com.clinic.user.repository;

import com.clinic.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByCorreo(String correo);
    Optional<User> findByKeycloakId(String keycloakId);
}
