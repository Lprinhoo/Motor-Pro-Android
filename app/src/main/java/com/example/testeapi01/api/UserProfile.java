package com.example.testeapi01.api;

public class UserProfile {
    private Long id;
    private String googleId;
    private String name;
    private String email;
    private String photoUrl;

    public UserProfile() {}

    public UserProfile(Long id, String googleId, String name, String email, String photoUrl) {
        this.id = id;
        this.googleId = googleId;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}