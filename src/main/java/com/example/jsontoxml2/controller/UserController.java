package com.example.jsontoxml2.controller;

import com.example.jsontoxml2.model.dto.user.UserSaveDto;
import com.example.jsontoxml2.model.dto.user.UserInfoDto;
import com.example.jsontoxml2.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserInfoDto>> getAllUsers() {
        List<UserInfoDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserInfoDto> addUser(@Valid @RequestBody UserSaveDto newUserSaveDto) {
        var savedUser = userService.addUser(newUserSaveDto);
        return ResponseEntity
                .created(URI.create(String.format("/users/%d", savedUser.getId())))
                .body(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePost(@PathVariable long id,
                                             @Valid @RequestBody UserSaveDto updatedUserSaveDto) {
        userService.updateUser(id, updatedUserSaveDto);
        return ResponseEntity.ok().body("Post updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePostById(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }

}
