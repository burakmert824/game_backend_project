package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.controller.exception.ResourceNotFoundException;
import com.dreamgames.backendengineeringcasestudy.dto.CountryLeaderboardDTO;
import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.entity.Tournament;
import com.dreamgames.backendengineeringcasestudy.entity.User;
import com.dreamgames.backendengineeringcasestudy.entity.UserTournament;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserTournamentRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;

import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TournamentServiceTest {

    @Mock
    private Clock clock;

    private Clock fixedClock;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTournamentRepository userTournamentRepository;

    @InjectMocks
    private TournamentService tournamentService;

    private final static LocalDate LOCAL_DATE = LocalDate.of(1989, 1, 13);
    private final static LocalTime TOURNAMENT_TIME = LocalTime.of(12, 0);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        //tell your tests to return the specified LOCAL_DATE when calling LocalDate.now(clock)
        fixedClock = Clock.fixed(
            LOCAL_DATE.atTime(TOURNAMENT_TIME).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());
        
        //current time 
        // LOCAL_DATE.atTime(12,0).atZone(ZoneId.systemDefault()).toInstant()
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    
    /**
     * Test for finding an eligible tournament.
     * Verifies that the correct tournament is returned based on the number of competitors.
     */
    @Test
    public void testFindEligibleTournament() {
        User user = new User();
        user.setCountry("Turkey");

        Tournament tournament1 = new Tournament();
        tournament1.setId(1L);
        Tournament tournament2 = new Tournament();
        tournament2.setId(2L);

        when(tournamentRepository.findEligibleTournaments(user.getCountry(), LOCAL_DATE)).thenReturn(Arrays.asList(tournament1, tournament2));
        when(userTournamentRepository.countByTournamentId(tournament1.getId())).thenReturn(5);
        when(userTournamentRepository.countByTournamentId(tournament2.getId())).thenReturn(3);

        Tournament result = tournamentService.findEligibleTournament(user, LOCAL_DATE);

        assertThat(result).isEqualTo(tournament2);
    }

    /**
     * Test for creating a new tournament.
     * Verifies that the tournament is saved and returned correctly.
     */
    @Test
    public void testCreateTournament() {
        Tournament tournament = new Tournament();
        tournament.setDate(LOCAL_DATE);

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        Tournament result = tournamentService.createTournament(LOCAL_DATE);

        assertThat(result).isEqualTo(tournament);
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    /**
     * Test for adding a user to a tournament.
     * Verifies that the user is added correctly and the list of competitors is updated.
     */
    @Test
    public void testAddUserToTournament() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setCountry("Turkey");
        user.setCoins(2000);

        Tournament tournament = new Tournament();
        tournament.setId(1L);

        List<TournamentCompetitorScoreDTO> competitors = new ArrayList<>();
        TournamentCompetitorScoreDTO existingCompetitor = new TournamentCompetitorScoreDTO(2L, "existinguser", "Turkey", 0);
        competitors.add(existingCompetitor);

        when(userTournamentRepository.findCompetitorsByTournamentIdOrderByScoreDesc(tournament.getId())).thenReturn(competitors);
        when(userTournamentRepository.save(any(UserTournament.class))).thenReturn(new UserTournament());

        List<TournamentCompetitorScoreDTO> result = tournamentService.addUserToTournament(user, tournament);

        assertThat(result).hasSize(2);
        assertThat(result).contains(existingCompetitor);
        assertThat(result).anyMatch(c -> c.getUserId().equals(user.getId()) && c.getUsername().equals(user.getUsername()) && c.getCountry().equals(user.getCountry()) && c.getScore() == 0);

        verify(userTournamentRepository, times(1)).save(any(UserTournament.class));
        verify(userRepository, times(1)).save(user);
        verify(tournamentRepository, never()).save(tournament);
    }

    /**
     * Test for checking if a user is participating in a tournament.
     * Verifies that the correct boolean value is returned.
     */
    @Test
    public void testIsUserParticipating() {
        User user = new User();
        user.setId(1L);

        when(userTournamentRepository.existsByUserIdAndTournamentDate(user.getId(), LOCAL_DATE)).thenReturn(true);

        boolean result = tournamentService.isUserParticipating(user, LOCAL_DATE);

        assertThat(result).isTrue();
        verify(userTournamentRepository, times(1)).existsByUserIdAndTournamentDate(user.getId(), LOCAL_DATE);
    }

    /**
     * Test for retrieving competitors by tournament ID.
     * Verifies that the correct list of competitors is returned.
     */
    @Test
    public void testGetCompetitorsByTournamentId() {
        Long tournamentId = 1L;
        List<TournamentCompetitorScoreDTO> competitors = Arrays.asList(new TournamentCompetitorScoreDTO(1L, "testuser", "Turkey", 100));

        when(userTournamentRepository.findCompetitorsByTournamentIdOrderByScoreDesc(tournamentId)).thenReturn(competitors);

        List<TournamentCompetitorScoreDTO> result = tournamentService.getCompetitorsByTournamentId(tournamentId);

        assertThat(result).isEqualTo(competitors);
        verify(userTournamentRepository, times(1)).findCompetitorsByTournamentIdOrderByScoreDesc(tournamentId);
    }

    /**
     * Test for retrieving active tournament participation.
     * Verifies that the correct UserTournament object is returned.
     */
    @Test
    public void testGetActiveTournamentParticipation() {
        Long userId = 1L;
        UserTournament userTournament = new UserTournament();

        when(userTournamentRepository.findActiveTournamentParticipation(userId, LOCAL_DATE)).thenReturn(userTournament);

        UserTournament result = tournamentService.getActiveTournamentParticipation(userId, LOCAL_DATE);

        assertThat(result).isEqualTo(userTournament);
        verify(userTournamentRepository, times(1)).findActiveTournamentParticipation(userId, LOCAL_DATE);
    }

    /**
     * Test for updating user tournament score.
     * Verifies that the score is updated correctly and the UserTournament object is returned.
     */
    @Test
    public void testUpdateUserTournamentScore() {
        UserTournament userTournament = new UserTournament();
        userTournament.setScore(10);

        when(userTournamentRepository.save(userTournament)).thenReturn(userTournament);

        UserTournament result = tournamentService.updateUserTournamentScore(userTournament, 5);

        assertThat(result.getScore()).isEqualTo(15);
        verify(userTournamentRepository, times(1)).save(userTournament);
    }

    /**
     * Test for checking unclaimed tournaments for a user.
     * Verifies that the correct boolean value is returned.
     */
    @Test
    public void testHasUnclaimedTournaments() {
        Long userId = 1L;
        UserTournament userTournament1 = new UserTournament();
        userTournament1.setTournament(new Tournament());
        userTournament1.getTournament().setDate(LOCAL_DATE.minusDays(1));
        UserTournament userTournament2 = new UserTournament();
        userTournament2.setTournament(new Tournament());
        userTournament2.getTournament().setDate(LOCAL_DATE);

        List<UserTournament> unclaimedTournaments = Arrays.asList(userTournament1, userTournament2);

        when(userTournamentRepository.findUnclaimedTournamentsByUserId(userId)).thenReturn(unclaimedTournaments);

        boolean result = tournamentService.hasUnclaimedTournaments(userId);

        assertThat(result).isTrue();
        verify(userTournamentRepository, times(1)).findUnclaimedTournamentsByUserId(userId);
    }

    /**
     * Test for retrieving a user's tournament.
     * Verifies that the correct UserTournament object is returned.
     */
    @Test
    public void testGetUserTournament() {
        Long userId = 1L;
        Long tournamentId = 1L;
        UserTournament userTournament = new UserTournament();

        when(userTournamentRepository.findByUserIdAndTournamentId(userId, tournamentId)).thenReturn(java.util.Optional.of(userTournament));

        UserTournament result = tournamentService.getUserTournament(userId, tournamentId);

        assertThat(result).isEqualTo(userTournament);
        verify(userTournamentRepository, times(1)).findByUserIdAndTournamentId(userId, tournamentId);
    }

    /**
     * Test for claiming a tournament prize.
     * Verifies that the prize is claimed correctly and the UserTournament object is updated.
     */
    @Test
    public void testClaimTournamentPrize() {
        Long userId = 1L;
        Long tournamentId = 1L;
        User user = new User();
        user.setId(userId);
        user.setCoins(500);
        Tournament tournament = new Tournament();
        UserTournament userTournament = new UserTournament(user, tournament, false, 0);

        when(userTournamentRepository.findByUserIdAndTournamentId(userId, tournamentId)).thenReturn(java.util.Optional.of(userTournament));
        when(userRepository.save(user)).thenReturn(user);
        when(userTournamentRepository.save(userTournament)).thenReturn(userTournament);

        UserTournament result = tournamentService.claimTournamentPrize(userId, tournamentId, 10000);

        assertThat(result.isClaimed()).isTrue();
        assertThat(result.getUser().getCoins()).isEqualTo(10500);
        verify(userTournamentRepository, times(1)).findByUserIdAndTournamentId(userId, tournamentId);
        verify(userRepository, times(1)).save(user);
        verify(userTournamentRepository, times(1)).save(userTournament);
    }

    /**
     * Test for retrieving the country leaderboard by date.
     * Verifies that the correct list of CountryLeaderboardDTO objects is returned.
     */
    @Test
    public void testGetCountryLeaderboardByDate() {
        Tournament tournament1 = new Tournament();
        tournament1.setId(1L);
        Tournament tournament2 = new Tournament();
        tournament2.setId(2L);

        when(tournamentRepository.findByDate(LOCAL_DATE)).thenReturn(Arrays.asList(tournament1, tournament2));

        List<CountryLeaderboardDTO> leaderboard1 = Arrays.asList(
            new CountryLeaderboardDTO("Turkey", 100L),
            new CountryLeaderboardDTO("USA", 90L)
        );
        List<CountryLeaderboardDTO> leaderboard2 = Arrays.asList(
            new CountryLeaderboardDTO("Turkey", 50L),
            new CountryLeaderboardDTO("Germany", 80L)
        );

        when(userTournamentRepository.findCountryLeaderboardByTournamentId(tournament1.getId())).thenReturn(leaderboard1);
        when(userTournamentRepository.findCountryLeaderboardByTournamentId(tournament2.getId())).thenReturn(leaderboard2);

        List<CountryLeaderboardDTO> result = tournamentService.getCountryLeaderboardByDate(LOCAL_DATE);

        assertThat(result).hasSize(3);
        assertThat(result).anyMatch(c -> c.getCountry().equals("Turkey") && c.getTotalScore() == 150L);
        assertThat(result).anyMatch(c -> c.getCountry().equals("USA") && c.getTotalScore() == 90L);
        assertThat(result).anyMatch(c -> c.getCountry().equals("Germany") && c.getTotalScore() == 80L);

        verify(tournamentRepository, times(1)).findByDate(LOCAL_DATE);
        verify(userTournamentRepository, times(1)).findCountryLeaderboardByTournamentId(tournament1.getId());
        verify(userTournamentRepository, times(1)).findCountryLeaderboardByTournamentId(tournament2.getId());
    }

    /**
     * Test for retrieving the country leaderboard by tournament ID.
     * Verifies that the correct list of CountryLeaderboardDTO objects is returned.
     */
    @Test
    public void testGetCountryLeaderboard() {
        Long tournamentId = 1L;
        List<CountryLeaderboardDTO> leaderboard = Arrays.asList(
            new CountryLeaderboardDTO("Turkey", 100L),
            new CountryLeaderboardDTO("USA", 90L)
        );

        when(userTournamentRepository.findCountryLeaderboardByTournamentId(tournamentId)).thenReturn(leaderboard);

        List<CountryLeaderboardDTO> result = tournamentService.getCountryLeaderboard(tournamentId);

        assertThat(result).isEqualTo(leaderboard);
        verify(userTournamentRepository, times(1)).findCountryLeaderboardByTournamentId(tournamentId);
    }

}
