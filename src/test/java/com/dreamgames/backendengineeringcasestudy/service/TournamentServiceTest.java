package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.entity.Tournament;
import com.dreamgames.backendengineeringcasestudy.entity.User;
import com.dreamgames.backendengineeringcasestudy.entity.UserTournament;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserTournamentRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;



import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTournamentRepository userTournamentRepository;

    @InjectMocks
    private TournamentService tournamentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindEligibleTournament() {
        User user = new User();
        user.setCountry("Turkey");

        Tournament tournament1 = new Tournament();
        tournament1.setId(1L);

        Tournament tournament2 = new Tournament();
        tournament2.setId(2L);

        List<Tournament> tournaments = Arrays.asList(tournament1, tournament2);

        when(tournamentRepository.findEligibleTournaments(anyString(), any(LocalDate.class))).thenReturn(tournaments);
        when(userTournamentRepository.countByTournamentId(1L)).thenReturn(3);
        when(userTournamentRepository.countByTournamentId(2L)).thenReturn(5);

        LocalDate currentDate = LocalDate.now();
        Tournament eligibleTournament = tournamentService.findEligibleTournament(user, currentDate);

        assertThat(eligibleTournament).isNotNull();
        assertThat(eligibleTournament.getId()).isEqualTo(1L);
    }

    @Test
    public void testCreateTournament() {
        LocalDate currentDate = LocalDate.now();

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setDate(currentDate);

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        Tournament createdTournament = tournamentService.createTournament(currentDate);

        assertThat(createdTournament).isNotNull();
        assertThat(createdTournament.getId()).isEqualTo(1L);
        assertThat(createdTournament.getDate()).isEqualTo(currentDate);
    }


    @Test
    public void testAddUserToTournament() {
        User user = new User();
        user.setId(1L);
        user.setCoins(5000);

        Tournament tournament = new Tournament();
        tournament.setId(1L);

        UserTournament userTournament = new UserTournament(user, tournament, false, 0);

        TournamentCompetitorScoreDTO competitor = new TournamentCompetitorScoreDTO();
        competitor.setUserId(1L);
        competitor.setUsername("testuser");
        competitor.setCountry("Turkey");
        competitor.setScore(100);

        List<TournamentCompetitorScoreDTO> competitors = new ArrayList<>(Collections.singletonList(competitor));

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userTournamentRepository.save(any(UserTournament.class))).thenReturn(userTournament);
        when(userTournamentRepository.findCompetitorsByTournamentIdOrderByScoreDesc(1L)).thenReturn(competitors);

        // Simulate adding the 4th user
        competitors = new ArrayList<>(Collections.nCopies(3, competitor));
        when(userTournamentRepository.findCompetitorsByTournamentIdOrderByScoreDesc(1L)).thenReturn(competitors);

        List<TournamentCompetitorScoreDTO> leaderboard = tournamentService.addUserToTournament(user, tournament);

        // Verify that the user's coins have been deducted
        assertThat(user.getCoins()).isEqualTo(4000);

        // Verify that the user and user-tournament relationship have been saved
        verify(userRepository, times(1)).save(user);
        verify(userTournamentRepository, times(1)).save(any(UserTournament.class));

        // Verify that the tournament's isStarted field has not been set to true yet
        assertThat(tournament.getIsStarted()).isFalse();

        // Verify that the leaderboard includes the added user
        assertThat(leaderboard.size()).isEqualTo(4);

        // Now simulate adding the 5th user
        competitors = new ArrayList<>(Collections.nCopies(4, competitor));
        when(userTournamentRepository.findCompetitorsByTournamentIdOrderByScoreDesc(1L)).thenReturn(competitors);

        leaderboard = tournamentService.addUserToTournament(user, tournament);

        // Verify that the tournament's isStarted field has been set to true
        assertThat(tournament.getIsStarted()).isTrue();
        verify(tournamentRepository, times(1)).save(tournament);

        // Verify that the leaderboard includes the added user
        assertThat(leaderboard.size()).isEqualTo(5);
    }
}
