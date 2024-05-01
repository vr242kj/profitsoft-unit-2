package com.example.jsontoxml2.repository.user;

import com.example.jsontoxml2.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
