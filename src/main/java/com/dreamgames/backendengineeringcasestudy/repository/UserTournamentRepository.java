package com.dreamgames.backendengineeringcasestudy.repository;


import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.entity.UserTournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;

import java.util.List;

@Repository
public interface UserTournamentRepository extends JpaRepository<UserTournament, Long> {

    int countByTournamentId(Long tournamentId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_tournament (user_id, tournament_id) VALUES (?1, ?2)", nativeQuery = true)
    void save(Long userId, Long tournamentId);

    @Query("SELECT COUNT(ut) > 0 FROM UserTournament ut WHERE ut.user.id = ?1 AND ut.tournament.date = ?2")
    boolean existsByUserIdAndTournamentDate(Long userId, LocalDate date);

    @Query("SELECT new com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO(u.id, u.username, u.country, ut.score) " +
           "FROM UserTournament ut " +
           "JOIN ut.user u " +
           "WHERE ut.tournament.id = :tournamentId " +
           "ORDER BY ut.score DESC")
    List<TournamentCompetitorScoreDTO> findCompetitorsByTournamentIdOrderByScoreDesc(Long tournamentId);

    @Query("SELECT ut FROM UserTournament ut WHERE ut.user.id = :userId AND ut.tournament.date = :date AND ut.tournament.isStarted = true")
    UserTournament findActiveTournamentParticipation(Long userId, LocalDate date);

}
