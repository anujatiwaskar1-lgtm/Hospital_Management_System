package com.medicore.controller;

import com.medicore.model.User;
import com.medicore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        boolean ok = userService.authenticate(username, password);
        return Map.of("success", ok, "username", username);
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User user) {
        if (userService.usernameExists(user.getUsername())) {
            return Map.of("message", "Username already taken");
        }
        userService.register(user);
        return Map.of("message", "Registered successfully");
    }
}