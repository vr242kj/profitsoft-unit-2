package com.example.jsontoxml2.repository;

import com.example.jsontoxml2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
