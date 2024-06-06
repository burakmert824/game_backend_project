package com.dreamgames.backendengineeringcasestudy.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_tournament")
public class UserTournament {

    @EmbeddedId
    private UserTournamentId id = new UserTournamentId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("tournamentId")
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    private boolean isClaimed;
    private int score;

    // Constructors
    public UserTournament() {}

    public UserTournament(User user, Tournament tournament, boolean isClaimed, int score) {
        this.user = user;
        this.tournament = tournament;
        this.isClaimed = isClaimed;
        this.score = score;
        this.id = new UserTournamentId(user.getId(), tournament.getId());
    }

    // Getters and setters
    public UserTournamentId getId() {
        return id;
    }

    public void setId(UserTournamentId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public boolean isClaimed() {
        return isClaimed;
    }

    public void setClaimed(boolean claimed) {
        isClaimed = claimed;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
