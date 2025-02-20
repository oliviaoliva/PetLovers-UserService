package com.example.user_service.controller;

import com.example.user_service.service.UserService;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pets.PetServiceGrpc;
import pets.Pets;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PetServiceGrpc.PetServiceBlockingStub petServiceStub;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, PetServiceGrpc.PetServiceBlockingStub petServiceStub) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.petServiceStub = petServiceStub;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }

    @PostMapping("/register")
public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("message", "E-mail já está em uso!"));
    }

    user.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepository.save(user);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(Collections.singletonMap("message", "Usuário registrado com sucesso!"));
}

    @PostMapping("/login")
public ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
    Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

    if (existingUser.isPresent()) {
        if (passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
            // Criar um objeto apenas com os dados necessários (sem senha)
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", existingUser.get().getId());
            userData.put("name", existingUser.get().getName());
            userData.put("email", existingUser.get().getEmail());
            userData.put("phone", existingUser.get().getPhone());
            userData.put("address", existingUser.get().getAddress());
            userData.put("username", existingUser.get().getUsername());

            return ResponseEntity.ok(userData);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "Senha incorreta!"));
        }
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Usuário não encontrado!"));
    }
}
    @GetMapping("/{userId}/pets")
    public ResponseEntity<List<String>> getUserPets(@PathVariable String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return ResponseEntity.ok(user.getPetIds()); // Retorna apenas os IDs dos pets do usuário
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/pets/{petId}")
    public ResponseEntity<Map<String, Object>> getPetById(@PathVariable String petId) {
        try {
            Pets.Pet response = petServiceStub.getPetById(Pets.PetIdRequest.newBuilder()
                    .setPetId(petId)
                    .build());

            // Retorna um JSON com os detalhes do pet
            Map<String, Object> petDetails = new HashMap<>();
            petDetails.put("id", response.getId());
            petDetails.put("name", response.getName());
            petDetails.put("breed", response.getBreed());
            petDetails.put("photo", response.getPhoto());
            petDetails.put("type", response.getType());
            petDetails.put("size", response.getSize());
            petDetails.put("sex", response.getSex());
            petDetails.put("neutered", response.getNeutered());

            return ResponseEntity.ok(petDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/{ownerId}/pets")
public ResponseEntity<?> createPet(@PathVariable String ownerId, @RequestBody Map<String, Object> petData) {
    try {
        String name = (String) petData.get("name");
        String breed = (String) petData.get("breed");
        // String ownerId = (String) petData.get("ownerId");
        String photo = (String) petData.get("photo");
        String type = (String) petData.get("type");
        String size = (String) petData.get("size");
        String sex = (String) petData.get("sex");
        Boolean neutered = (Boolean) petData.get("neutered");

        Pets.AddPetResponse createdPet = petServiceStub.addPet(
            Pets.AddPetRequest.newBuilder()
                .setName(name)
                .setBreed(breed)
                .setPhoto(photo)
                .setType(type)
                .setSize(size)
                .setSex(sex)
                .setNeutered(neutered)
                .setOwnerId(ownerId)
                .build()
        );
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", createdPet.getId());
        responseBody.put("name", createdPet.getName());
        responseBody.put("breed", createdPet.getBreed());

        return ResponseEntity.ok(responseBody);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao criar pet: " + e.getMessage());
    }
  }
}