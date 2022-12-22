package com.karl.pfind.Models;

public class Owner {

    private String name;
    private String email;
    private String username;
    private String password;
    private String phoneNumber;
    private Boolean isPetMissing;
    private CustomLocation lastLocation;

    public Owner() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getPetMissing() {
        return isPetMissing;
    }

    public void setPetMissing(Boolean petMissing) {
        isPetMissing = petMissing;
    }

    public CustomLocation getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(CustomLocation lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
