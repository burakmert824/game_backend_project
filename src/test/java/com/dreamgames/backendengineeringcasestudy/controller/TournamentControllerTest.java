package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
import com.dreamgames.backendengineeringcasestudy.dto.CountryLeaderboardDTO;
import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TournamentControllerTest {

    @Mock
    private TournamentService tournamentService;

    @InjectMocks
    private TournamentController tournamentController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test for getCompetitorsByTournamentId.
     * Verifies that the method returns a list of competitors for the given tournament ID.
     * Ensures that the correct response status and data are returned.
     */
    @Test
    public void testGetCompetitorsByTournamentId() {
        Long tournamentId = 1L;
        List<TournamentCompetitorScoreDTO> competitors = new ArrayList<>();
        TournamentCompetitorScoreDTO competitor = new TournamentCompetitorScoreDTO();
        competitor.setUserId(1L);
        competitor.setUsername("testuser");
        competitor.setCountry("Turkey");
        competitor.setScore(100);
        competitors.add(competitor);

        when(tournamentService.getCompetitorsByTournamentId(tournamentId)).thenReturn(competitors);

        ResponseEntity<ApiResponse<List<TournamentCompetitorScoreDTO>>> responseEntity = tournamentController.getCompetitorsByTournamentId(tournamentId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Leadboard retrieved successfully");
        assertThat(responseEntity.getBody().getData()).isEqualTo(competitors);

        verify(tournamentService, times(1)).getCompetitorsByTournamentId(tournamentId);
    }

     /**
     * Test for getCountryLeaderboard.
     * Verifies that the method returns the country leaderboard for the given tournament ID.
     * Ensures that the correct response status and data are returned.
     */
    @Test
    public void testGetCountryLeaderboard() {
        Long tournamentId = 1L;
        List<CountryLeaderboardDTO> leaderboard = new ArrayList<>();
        CountryLeaderboardDTO countryLeaderboard = new CountryLeaderboardDTO();
        countryLeaderboard.setCountry("Turkey");
        leaderboard.add(countryLeaderboard);

        when(tournamentService.getCountryLeaderboard(tournamentId)).thenReturn(leaderboard);

        ResponseEntity<ApiResponse<List<CountryLeaderboardDTO>>> responseEntity = tournamentController.getCountryLeaderboard(tournamentId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Country leaderboard retrieved successfully");
        assertThat(responseEntity.getBody().getData()).isEqualTo(leaderboard);

        verify(tournamentService, times(1)).getCountryLeaderboard(tournamentId);
    }

}
