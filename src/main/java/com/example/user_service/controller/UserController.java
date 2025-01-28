package com.example.user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pets.PetServiceGrpc;
import pets.Pets; // Classes geradas pelo gRPC (pets.proto)
import com.example.user_service.dto.PetDTO;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {
    // Atributo para o stub gRPC
    private final PetServiceGrpc.PetServiceBlockingStub petServiceStub;

    // Construtor para injeção de dependência (Spring)
    public UserController(PetServiceGrpc.PetServiceBlockingStub petServiceStub) {
        // "petServiceStub" vem do @Bean definido em GrpcConfig
        this.petServiceStub = petServiceStub;
    }

    @GetMapping("/pets")
    public List<PetDTO> listPets() {
        // Chamar o Pet Service via gRPC
        Pets.PetsList response = petServiceStub.listPets(Pets.Empty.getDefaultInstance());

        // Converter a lista de "Pets" para uma lista de "PetDTO"
        return response.getPetsList().stream()
                .map(p -> new PetDTO(p.getId(), p.getName(), p.getBreed()))
                .collect(Collectors.toList());
    }
}
