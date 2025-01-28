package com.example.user_service.dto;

public class PetDTO {
    private String id;
    private String name;
    private String breed;

    // Construtor
    public PetDTO(String id, String name, String breed) {
        this.id = id;
        this.name = name;
        this.breed = breed;
    }

    // Getters e setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }
    public void setBreed(String breed) {
        this.breed = breed;
    }
}
