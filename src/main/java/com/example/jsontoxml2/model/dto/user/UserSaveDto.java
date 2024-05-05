package com.example.jsontoxml2.model.dto.user;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSaveDto {

    @Column(name = "username", unique = true)
    @NotBlank(message = "Name is required")
    private String username;

    @Email
    @Column(name = "email", unique = true)
    @NotBlank(message = "Email is required")
    private String email;

}
