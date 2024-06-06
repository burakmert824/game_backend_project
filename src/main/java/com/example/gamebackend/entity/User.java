package com.example.gamebackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class User { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private int level = 1;
    private int coints = 5000;
    private String country;


    public User() {
    }


    public Long getId() {
        return id;
    }


    public String getUsername() {
        return username;
    }


    public int getLevel() {
        return level;
    }


    public int getCoints() {
        return coints;
    }


    public String getCountry() {
        return country;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public void setLevel(int level) {
        this.level = level;
    }


    public void setCoints(int coints) {
        this.coints = coints;
    }


    public void setCountry(String country) {
        this.country = country;
    }

    
    
}