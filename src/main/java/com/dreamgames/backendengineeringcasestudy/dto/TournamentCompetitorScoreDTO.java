package com.dreamgames.backendengineeringcasestudy.dto;

//it is used for showing tournament scoreboard
public class TournamentCompetitorScoreDTO {
    private Long userId;
    private String username;
    private String country;
    private int score;

    public TournamentCompetitorScoreDTO(Long userId, String username, String country, int score) {
        this.userId = userId;
        this.username = username;
        this.country = country;
        this.score = score;
    }
    
    public TournamentCompetitorScoreDTO() {
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
