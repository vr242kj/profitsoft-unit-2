package com.example.jsontoxml2.controller;

import com.example.jsontoxml2.entity.User;
import com.example.jsontoxml2.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> addUser(@Validated @RequestBody User newUser) {
        var savedUser = userService.addUser(newUser);
        return ResponseEntity
                .created(URI.create(String.format("/users/%d", savedUser.getId())))
                .body(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(@PathVariable long id, @Validated @RequestBody User updateUser) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }

        if (updateUser.getEmail() != null) {
            existingUser.setEmail(updateUser.getEmail());
        }
        if (updateUser.getUsername() != null) {
            existingUser.setUsername(updateUser.getUsername());
        }
        userService.addUser(existingUser);
        return ResponseEntity.ok().body("Post updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePostById(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }

}
