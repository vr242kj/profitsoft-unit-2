package com.example.jsontoxml2.model.dto.user;

import com.example.jsontoxml2.model.dto.post.PostDTO;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDTO {

    private Long id;

    @Column(name = "username", unique = true)
    @NotBlank(message = "Name is required")
    private String username;

    @Email
    @Column(name = "email", unique = true)
    @NotBlank(message = "Email is required")
    private String email;

    List<PostDTO> userPosts = new ArrayList<>();
}
