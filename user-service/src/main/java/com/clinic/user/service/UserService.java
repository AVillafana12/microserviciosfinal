package com.clinic.user.service;

import com.clinic.user.model.User;
import com.clinic.user.model.UserRole;
import com.clinic.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User crearUsuario(User user) {
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }
        return repository.save(user);
    }

    public User crearOActualizarDesdeKeycloak(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new RuntimeException("Invalid authentication");
        }

        String keycloakId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String name = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");

        Optional<User> existingUser = repository.findByKeycloakId(keycloakId);
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Crear nuevo usuario desde Keycloak
        User newUser = User.builder()
                .keycloakId(keycloakId)
                .correo(email != null ? email : "no-email@clinic.com")
                .nombre(name != null ? name : "Sin nombre")
                .apellido(lastName != null ? lastName : "Sin apellido")
                .role(UserRole.USER)
                .build();

        return repository.save(newUser);
    }

    public List<User> obtenerUsuarios() {
        return repository.findAll();
    }

    public User obtenerPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public User obtenerPorKeycloakId(String keycloakId) {
        return repository.findByKeycloakId(keycloakId).orElse(null);
    }

    public User obtenerPorCorreo(String correo) {
        return repository.findByCorreo(correo).orElse(null);
    }

    public Integer obtenerUserIdDesdeAuth(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new RuntimeException("Invalid authentication");
        }

        String keycloakId = jwt.getSubject();
        User user = repository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return user.getId();
    }
}
