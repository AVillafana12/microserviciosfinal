package com.clinic.user.controller;

import com.clinic.user.model.User;
import com.clinic.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<User> byId(@PathVariable String id) {
        var u = service.obtenerPorId(id);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }
}
