package com.example.user_service.dto;

public class FavoritePetDTO {
    private String id;
    private String name;
    private String breed;
    private String photo;
    private String type;
    private String size;
    private String sex;
    private boolean neutered;

    public FavoritePetDTO(String id, String name, String breed, String photo, String type, String size, String sex, boolean neutered) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.photo = photo;
        this.type = type;
        this.size = size;
        this.sex = sex;
        this.neutered = neutered;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public boolean isNeutered() { return neutered; }
    public void setNeutered(boolean neutered) { this.neutered = neutered; }
}
