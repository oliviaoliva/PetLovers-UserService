package com.example.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.user_service.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}