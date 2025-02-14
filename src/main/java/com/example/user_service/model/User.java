package com.example.user_service.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) 
    private String id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @ElementCollection
    @CollectionTable(name = "user_pet_ids", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "pet_id")
    private List<String> petIds = new ArrayList<>();

    @ElementCollection
    private List<String> favoritePets = new ArrayList<>();

}
