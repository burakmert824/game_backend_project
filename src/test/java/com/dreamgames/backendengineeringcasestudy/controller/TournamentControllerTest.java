package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.controller.exception.ResourceNotFoundException;
import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
import com.dreamgames.backendengineeringcasestudy.dto.CountryLeaderboardDTO;
import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import java.util.Collections;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TournamentControllerTest {

    // Some fixed date to make your tests
    private final static LocalDate LOCAL_DATE = LocalDate.of(1989, 01, 13);
    
    //Mock your clock bean
    @Mock
    private Clock clock;

    //field that will contain the fixed clock
    private Clock fixedClock;

    @Mock
    private TournamentService tournamentService;

    @InjectMocks
    private TournamentController tournamentController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        //tell your tests to return the specified LOCAL_DATE when calling LocalDate.now(clock)
        fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }

 
    @Test
    public void testGetCompetitorsByTournamentId() {
        Long tournamentId = 1L;
        List<TournamentCompetitorScoreDTO> competitors = Collections.singletonList(
                new TournamentCompetitorScoreDTO(1L, "testuser", "Turkey", 100)
        );

        when(tournamentService.getCompetitorsByTournamentId(tournamentId)).thenReturn(competitors);

        ResponseEntity<ApiResponse<List<TournamentCompetitorScoreDTO>>> responseEntity = tournamentController.getCompetitorsByTournamentId(tournamentId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Leadboard retrieved successfully");
        assertThat(responseEntity.getBody().getData()).isEqualTo(competitors);

        verify(tournamentService, times(1)).getCompetitorsByTournamentId(tournamentId);
    }

    @Test
    public void testGetCountryLeaderboard_Success() {
        LocalDate date = LocalDate.of(2023, 6, 1);
        List<CountryLeaderboardDTO> leaderboard = Arrays.asList(
                new CountryLeaderboardDTO("Turkey", 100L),
                new CountryLeaderboardDTO("United States", 90L),
                new CountryLeaderboardDTO("United Kingdom", 80L)
        );

        when(tournamentService.getCountryLeaderboardByDate(date)).thenReturn(leaderboard);

        ResponseEntity<ApiResponse<List<CountryLeaderboardDTO>>> response = tournamentController.getCountryLeaderboard(date);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Country leaderboard retrieved successfully");
        assertThat(response.getBody().getData()).isEqualTo(leaderboard);

        verify(tournamentService, times(1)).getCountryLeaderboardByDate(date);
    }

    @Test
    public void testGetCountryLeaderboard_NoTournamentsFound() {
        LocalDate date = LocalDate.of(2023, 6, 1);

        when(tournamentService.getCountryLeaderboardByDate(date)).thenReturn(Collections.emptyList());

        try {
            tournamentController.getCountryLeaderboard(date);
        } catch (ResourceNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("No tournaments found on date: " + date);
        }

        verify(tournamentService, times(1)).getCountryLeaderboardByDate(date);
    }

}
