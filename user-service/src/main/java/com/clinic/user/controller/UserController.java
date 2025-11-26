package com.clinic.user.controller;

import com.clinic.user.model.User;
import com.clinic.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
 
    @PostMapping
    public ResponseEntity<User> crear(@RequestBody User user) {
        return ResponseEntity.ok(service.crearUsuario(user));
    }

    @GetMapping
    public ResponseEntity<List<User>> all() {
        return ResponseEntity.ok(service.obtenerUsuarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> byId(@PathVariable Integer id) {
        var u = service.obtenerPorId(id);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        try {
            User user = service.crearOActualizarDesdeKeycloak(authentication);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-id")
    public ResponseEntity<Integer> getMyUserId(Authentication authentication) {
        try {
            Integer userId = service.obtenerUserIdDesdeAuth(authentication);
            return ResponseEntity.ok(userId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
