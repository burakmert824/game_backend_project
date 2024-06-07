package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    @Query("SELECT t FROM Tournament t WHERE t.id NOT IN " +
           "(SELECT ut.tournament.id FROM UserTournament ut JOIN ut.user u WHERE u.country = ?1) " +
           "AND t.date = ?2")
    List<Tournament> findEligibleTournaments(String country, LocalDate date);

    List<Tournament> findByDate(LocalDate date);

}
