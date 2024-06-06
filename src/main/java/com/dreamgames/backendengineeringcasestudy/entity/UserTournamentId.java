package com.dreamgames.backendengineeringcasestudy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserTournamentId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "tournament_id")
    private Long tournamentId;

    // Constructors
    public UserTournamentId() {}

    public UserTournamentId(Long userId, Long tournamentId) {
        this.userId = userId;
        this.tournamentId = tournamentId;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    // Equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTournamentId that = (UserTournamentId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(tournamentId, that.tournamentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, tournamentId);
    }
}
