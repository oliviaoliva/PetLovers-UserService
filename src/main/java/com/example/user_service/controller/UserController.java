package com.example.user_service.controller;

import com.example.user_service.model.User;
import com.example.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import pets.PetServiceGrpc;
import pets.Pets;
import com.example.user_service.dto.PetDTO;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    private final PetServiceGrpc.PetServiceBlockingStub petServiceStub;

    public UserController(PetServiceGrpc.PetServiceBlockingStub petServiceStub) {
        this.petServiceStub = petServiceStub;
    }

    /** 🔹 CRUD de Usuários **/

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    /** 🔹 Comunicação com Pet Service **/

    @GetMapping("/pets")
    public List<PetDTO> listPets() {
        Pets.PetsList response = petServiceStub.listPets(Pets.Empty.getDefaultInstance());
        return response.getPetsList().stream()
                .map(p -> new PetDTO(p.getId(), p.getName(), p.getBreed()))
                .collect(Collectors.toList());
    }
}
