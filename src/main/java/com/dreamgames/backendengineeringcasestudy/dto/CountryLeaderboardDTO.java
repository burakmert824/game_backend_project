package com.dreamgames.backendengineeringcasestudy.dto;

public class CountryLeaderboardDTO {
    private String country;
    private Long totalScore;

    public CountryLeaderboardDTO(String country, Long totalScore) {
        this.country = country;
        this.totalScore = totalScore;
    }
    //empty constructor
    public CountryLeaderboardDTO() {
    }
    

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Long totalScore) {
        this.totalScore = totalScore;
    }
}

