package com.example.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.user_service.dto.FavoriteRequest;
import com.example.user_service.model.User;
import com.example.user_service.repository.*;

import pets.PetServiceGrpc;
import pets.Pets;
import java.util.*;

@RestController
@RequestMapping("/pets")
public class PetsController {

    @Autowired
    private PetServiceGrpc.PetServiceBlockingStub petServiceStub;
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    public PetsController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllPets() {
        try {
            Pets.PetsList response = petServiceStub.listPets(Pets.Empty.getDefaultInstance());
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
            return ResponseEntity.status(500).body("Erro ao listar pets: " + e.getMessage());
        }
    }

    @PutMapping("/{petId}")
    public ResponseEntity<?> updatePet(@PathVariable String petId, @RequestBody Map<String, Object> petData) {
        try {
            String name = (String) petData.get("name");
            String breed = (String) petData.get("breed");
            String photo = (String) petData.get("photo");
            String type = (String) petData.get("type");
            String size = (String) petData.get("size");
            String sex = (String) petData.get("sex");
            Boolean neutered = (Boolean) petData.get("neutered");

            Pets.Pet updatedPet = petServiceStub.updatePet(
                Pets.UpdatePetRequest.newBuilder()
                    .setId(petId)
                    .setName(name != null ? name : "")
                    .setBreed(breed != null ? breed : "")
                    .setPhoto(photo != null ? photo : "")
                    .setType(type != null ? type : "")
                    .setSize(size != null ? size : "")
                    .setSex(sex != null ? sex : "")
                    .setNeutered(neutered != null ? neutered : false)
                    .build()
            );

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("id", updatedPet.getId());
            responseBody.put("name", updatedPet.getName());
            responseBody.put("breed", updatedPet.getBreed());
            responseBody.put("photo", updatedPet.getPhoto());
            responseBody.put("type", updatedPet.getType());
            responseBody.put("size", updatedPet.getSize());
            responseBody.put("sex", updatedPet.getSex());
            responseBody.put("neutered", updatedPet.getNeutered());

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao atualizar pet: " + e.getMessage());
        }
    }

    @DeleteMapping("/{petId}")
    public ResponseEntity<?> deletePet(@PathVariable String petId) {
        try {
            petServiceStub.deletePet(
                Pets.DeletePetRequest.newBuilder()
                    .setId(petId)
                    .build()
            );
            return ResponseEntity.ok("Pet deletado com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao deletar pet: " + e.getMessage());
        }
    }
        
    @PostMapping("/{userId}/favorites")
    public ResponseEntity<?> favoritePet(@PathVariable String userId, @RequestBody FavoriteRequest request) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            System.out.println("❌ Usuário não encontrado: " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }

        User user = userOptional.get();

        if (user.getFavoritePets().contains(request.getPetId())) {
            System.out.println("⚠ Pet já favoritado pelo usuário: " + userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pet já está favoritado.");
        }

        Pets.Pet pet = petServiceStub.getPetById(Pets.PetIdRequest.newBuilder()
                .setPetId(request.getPetId())
                .build());

        System.out.println("📥 Recebendo pet do gRPC: " + pet);

        if (pet.getId().isEmpty()) {
            System.out.println("❌ Pet não encontrado no Pet Service: " + request.getPetId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet não encontrado.");
        }

        System.out.println("🔍 Comparando ownerId do pet: " + pet.getOwnerId() + " com userId: " + userId);

        if (pet.getOwnerId().equals(userId)) {
            System.out.println("🚫 Tentativa de favoritar próprio pet bloqueada! Usuário: " + userId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Você não pode favoritar seu próprio pet.");
        }

        user.getFavoritePets().add(request.getPetId());
        userRepository.save(user);
        System.out.println("✅ Pet favoritado com sucesso pelo usuário: " + userId);

        return ResponseEntity.ok(Collections.singletonMap("message", "Pet favoritado com sucesso!"));
    }

    @DeleteMapping("/{userId}/favorites/{petId}")
    public ResponseEntity<?> unfavoritePet(@PathVariable String userId, @PathVariable String petId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
        }
        User user = userOptional.get();

        if (!user.getFavoritePets().contains(petId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Este pet não estava favoritado.");
        }

        user.getFavoritePets().remove(petId);
        userRepository.save(user);

        return ResponseEntity.ok(Collections.singletonMap("message", "Pet desfavoritado com sucesso!"));
    }

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

            if (!pet.getId().isEmpty()) { 
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


