package com.clinic.user.service;

import com.clinic.user.model.User;
import com.clinic.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User crearUsuario(User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }
        return repository.save(user);
    }

    public List<User> obtenerUsuarios() {
        return repository.findAll();
    }

    public User obtenerPorId(String id) {
        return repository.findById(id).orElse(null);
    }
}