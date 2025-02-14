package com.example.user_service.controller;

import com.example.user_service.service.UserService;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.dto.FavoriteRequest;
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
import java.util.ArrayList;
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

@RestController
@RequestMapping("/pets")
public class PetsController {

    @Autowired
    private PetServiceGrpc.PetServiceBlockingStub petServiceStub;

    @GetMapping
    public ResponseEntity<?> getAllPets() {
        try {
            // Chama listPets do Pet Service via gRPC
            Pets.PetsList response = petServiceStub.listPets(Pets.Empty.getDefaultInstance());

        // Converter cada Pets.Pet em Map<String, Object> para Jackson serializar sem erro
        List<Map<String, Object>> petList = new ArrayList<>();
        for (Pets.Pet protoPet : response.getPetsList()) {
            Map<String, Object> petMap = new HashMap<>();
            petMap.put("id", protoPet.getId());
            petMap.put("name", protoPet.getName());
            petMap.put("breed", protoPet.getBreed());
            petMap.put("photo", protoPet.getPhoto());
            petMap.put("type", protoPet.getType());
            petMap.put("size", protoPet.getSize());
            petMap.put("sex", protoPet.getSex());
            petMap.put("neutered", protoPet.getNeutered());
            petMap.put("ownerId", protoPet.getOwnerId());
            petList.add(petMap);
        }

        return ResponseEntity.ok(petList);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao listar pets: " + e.getMessage());
    }
}
    @DeleteMapping("/{petId}")
    public ResponseEntity<?> deletePet(@PathVariable String petId) {
        try {
            // Chamando o Pet Service via gRPC
            petServiceStub.deletePet(
                Pets.DeletePetRequest.newBuilder()
                    .setId(petId)
                    .build()
            );

            return ResponseEntity.ok("Pet deletado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao deletar pet: " + e.getMessage());
        }
    }
}



// 🔹 Endpoint para favoritar um pet
@PostMapping("/{userId}/favorites")
public ResponseEntity<?> favoritePet(@PathVariable String userId, @RequestBody FavoriteRequest request) {
    Optional<User> userOptional = userRepository.findById(userId);
    if (!userOptional.isPresent()) {
        System.out.println("❌ Usuário não encontrado: " + userId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
    }

    User user = userOptional.get();

    // Log para verificar se o pet já foi favoritado
    if (user.getFavoritePets().contains(request.getPetId())) {
        System.out.println("⚠ Pet já favoritado pelo usuário: " + userId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pet já está favoritado.");
    }

    // Chamar o Pet Service via gRPC para obter detalhes do pet
    Pets.Pet pet = petServiceStub.getPetById(Pets.PetIdRequest.newBuilder()
            .setPetId(request.getPetId())
            .build());

    System.out.println("📥 Recebendo pet do gRPC: " + pet);

    if (pet.getId().isEmpty()) {
        System.out.println("❌ Pet não encontrado no Pet Service: " + request.getPetId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet não encontrado.");
    }

    // Log do `ownerId` para verificar se a validação está correta
    System.out.println("🔍 Comparando ownerId do pet: " + pet.getOwnerId() + " com userId: " + userId);

    //Verificar se o pet pertence ao usuário
    if (pet.getOwnerId().equals(userId)) {
        System.out.println("🚫 Tentativa de favoritar próprio pet bloqueada! Usuário: " + userId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Você não pode favoritar seu próprio pet.");
    }

    // Adicionar o pet à lista de favoritos
    user.getFavoritePets().add(request.getPetId());
    userRepository.save(user);
    System.out.println("✅ Pet favoritado com sucesso pelo usuário: " + userId);

    return ResponseEntity.ok("Pet favoritado com sucesso!");
}


    // listar os pets favoritos do usuário
    @GetMapping("/{userId}/favorites")
    public ResponseEntity<List<String>> getFavoritePets(@PathVariable String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(userOptional.get().getFavoritePets());
    }

    // detalhes pets
    @GetMapping("/{userId}/favorites/details")
    public ResponseEntity<?> getFavoritePetsDetails(@PathVariable String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }

        User user = userOptional.get();
        List<String> favoritePetIds = user.getFavoritePets();

        if (favoritePetIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList()); // 🔹 Retorna lista vazia se não houver favoritos
        }

        // 🔹 Buscar detalhes dos pets favoritos via gRPC
        List<Map<String, String>> favoritePetsDetails = new ArrayList<>();
        
        for (String petId : favoritePetIds) {
            Pets.Pet pet = petServiceStub.getPetById(Pets.PetIdRequest.newBuilder()
                    .setPetId(petId)
                    .build());

            if (!pet.getId().isEmpty()) { // Se o pet existir, adiciona os detalhes à lista
                Map<String, String> petDetails = new HashMap<>();
                petDetails.put("id", pet.getId());
                petDetails.put("name", pet.getName());
                petDetails.put("photo", pet.getPhoto());
                petDetails.put("type", pet.getType());
                petDetails.put("breed", pet.getBreed());
                petDetails.put("size", pet.getSize());
                petDetails.put("sex", pet.getSex());
                petDetails.put("neutered", String.valueOf(pet.getNeutered()));

                favoritePetsDetails.add(petDetails);
            }
        }

        return ResponseEntity.ok(favoritePetsDetails);
    }

}