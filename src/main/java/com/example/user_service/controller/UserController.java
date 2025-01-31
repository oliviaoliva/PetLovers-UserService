package com.example.user_service.controller;

import com.example.user_service.service.UserService;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.dto.PetDTO;
import com.example.user_service.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pets.PetServiceGrpc;
import pets.Pets;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /** CRUD de Usuários **/

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    /** Método de Cadastro de Usuário **/

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("E-mail já está em uso!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Salvar usuário no banco
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso!");
    }

    /** Comunicação com Pet Service **/

    @GetMapping("/pets")
    public List<PetDTO> listPets() {
        Pets.PetsList response = petServiceStub.listPets(Pets.Empty.getDefaultInstance());
        return response.getPetsList().stream()
                .map(p -> new PetDTO(p.getId(), p.getName(), p.getBreed()))
                .collect(Collectors.toList());
    }

    @PostMapping("/pets")
    public ResponseEntity<?> addPet(@RequestBody PetDTO petDTO) {
        try {
            Pets.AddPetResponse response = petServiceStub.addPet(Pets.AddPetRequest.newBuilder()
                    .setName(petDTO.getName())
                    .setBreed(petDTO.getBreed())
                    .build());

            return ResponseEntity.ok(new PetDTO(response.getId(), response.getName(), response.getBreed()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao adicionar pet: " + e.getMessage());
        }
    }

    /** Login de Usuário **/

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            if (passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
                return ResponseEntity.ok("Login bem-sucedido!");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha incorreta!");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
        }
    }
}
