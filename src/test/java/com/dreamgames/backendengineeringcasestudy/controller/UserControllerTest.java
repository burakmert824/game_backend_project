package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.controller.exception.AlreadyInTournamentException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.InsufficientCoinsException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.NoTournamentAtThisHourException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.NotEnoughCompetitorsException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.PrizeAlreadyClaimedException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.ResourceNotFoundException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.TournamentNotEndedException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.UnclaimedTournamentException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.UserAlreadyExistsException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.UserNotEligibleException;
import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.entity.Tournament;
import com.dreamgames.backendengineeringcasestudy.entity.User;
import com.dreamgames.backendengineeringcasestudy.entity.UserTournament;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import java.util.Random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class UserControllerTest {


    // Some fixed date to make your tests
    private final static LocalDate LOCAL_DATE = LocalDate.of(1989, 01, 13);

    private final static LocalTime OUT_TOURNAMENT_TIME = LocalTime.of(23,0);
    private final static LocalTime IN_TOURNAMENT_TIME = LocalTime.of(12,0);


    //Mock your clock bean
    @Mock
    private Clock clock;

    //field that will contain the fixed clock
    private Clock fixedClock;

    @Mock
    private UserService userService;

    @Mock
    private TournamentService tournamentService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        //tell your tests to return the specified LOCAL_DATE when calling LocalDate.now(clock)
        fixedClock = Clock.fixed(
            LOCAL_DATE.atTime(12,0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());
        
        //current time 
        // LOCAL_DATE.atTime(12,0).atZone(ZoneId.systemDefault()).toInstant()
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    /**
     * Test for createUser.
     * Verifies that a new user is created with default values.
     * Ensures that the correct response status and data are returned.
     */
    @Test
    public void testCreateUser() {
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setCoins(5000);
        user.setLevel(1);

        String[] countries = {"Turkey", "United States", "United Kingdom", "France", "Germany"};
        Random random = new Random();
        String randomCountry = countries[random.nextInt(countries.length)];
        user.setCountry(randomCountry);

        when(userService.existsByUsername(username)).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(user);

        ResponseEntity<ApiResponse<User>> responseEntity = userController.createUser(username);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User created successfully");
        assertThat(responseEntity.getBody().getData().getUsername()).isEqualTo(username);
        assertThat(responseEntity.getBody().getData().getCoins()).isEqualTo(5000);
        assertThat(responseEntity.getBody().getData().getLevel()).isEqualTo(1);
        assertThat(responseEntity.getBody().getData().getCountry()).isEqualTo(randomCountry);

        verify(userService, times(1)).existsByUsername(username);
        verify(userService, times(1)).createUser(any(User.class));
    }

    /**
     * Test for createUser when username already exists.
     * Verifies that a conflict response is returned when the username already exists.
     */
    @Test
    public void testCreateUserWhenUsernameAlreadyExists() {
        String username = "existinguser";

        when(userService.existsByUsername(username)).thenReturn(true);

        try {
            userController.createUser(username);
        } catch (UserAlreadyExistsException ex) {
            assertThat(ex.getMessage()).isEqualTo("User already exists with username: " + username);
        }

        verify(userService, times(1)).existsByUsername(username);
        verify(userService, times(0)).createUser(any(User.class));
    }


   /**
     * Test for incrementUserLevel when within tournament hours.
     * Verifies that the user's level is incremented by one and 25 coins are added.
     * Checks if the user's tournament score is updated.
     */
    @Test
    public void testIncrementUserLevelWithinTournamentHours() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(10);
        user.setCoins(500);

        UserTournament userTournament = new UserTournament(user, new Tournament(), false, 0);

        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.updateUser(any(User.class))).thenReturn(user);
        when(tournamentService.getActiveTournamentParticipation(userId, LOCAL_DATE)).thenReturn(userTournament);
        when(tournamentService.updateUserTournamentScore(userTournament, 1)).thenReturn(userTournament);

        ResponseEntity<ApiResponse<UserTournament>> responseEntity = userController.incrementUserLevel(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User level incremented successfully");
        assertThat(responseEntity.getBody().getData()).isEqualTo(userTournament);
        assertThat(user.getLevel()).isEqualTo(11);
        assertThat(user.getCoins()).isEqualTo(525);

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).updateUser(any(User.class));
        verify(tournamentService, times(1)).getActiveTournamentParticipation(userId, LOCAL_DATE);
        verify(tournamentService, times(1)).updateUserTournamentScore(userTournament, 1);
    }

    /**
     * Test for incrementUserLevel when outside tournament hours.
     * Verifies that the user's level is incremented by one and 25 coins are added.
     * Ensures that the user's tournament score is not updated.
     */
    @Test
    public void testIncrementUserLevelOutsideTournamentHours() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(10);
        user.setCoins(500);

        fixedClock = Clock.fixed(
            LOCAL_DATE.atTime(OUT_TOURNAMENT_TIME).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.updateUser(any(User.class))).thenReturn(user);
        when(tournamentService.getActiveTournamentParticipation(userId, LOCAL_DATE)).thenReturn(null);

        ResponseEntity<ApiResponse<UserTournament>> responseEntity = userController.incrementUserLevel(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User level incremented successfully");
        assertThat(responseEntity.getBody().getData().getUser()).isEqualTo(user);
        assertThat(user.getLevel()).isEqualTo(11);
        assertThat(user.getCoins()).isEqualTo(525);

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).updateUser(any(User.class));
        verify(tournamentService, times(1)).getActiveTournamentParticipation(userId, LOCAL_DATE);
        verify(tournamentService, times(0)).updateUserTournamentScore(any(UserTournament.class), anyInt());
    }

    /**
     * Test for incrementUserLevel when user not found.
     * Verifies that a ResourceNotFoundException is thrown.
     */
    @Test
    public void testIncrementUserLevelUserNotFound() {
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(null);

        try {
            userController.incrementUserLevel(userId);
        } catch (ResourceNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("User not found with id: " + userId);
        }

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(0)).updateUser(any(User.class));
        verify(tournamentService, times(0)).getActiveTournamentParticipation(userId, LOCAL_DATE);
        verify(tournamentService, times(0)).updateUserTournamentScore(any(UserTournament.class), anyInt());
    }

    /**
     * Test for incrementUserLevel when there is no attended tournament for the user.
     * Verifies that the user's level is incremented by one and 25 coins are added.
     * Ensures that the user's tournament score is not updated and no tournament data is returned.
     */
    @Test
    public void testIncrementUserLevelNoAttendedTournament() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(10);
        user.setCoins(500);

        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.updateUser(any(User.class))).thenReturn(user);
        when(tournamentService.getActiveTournamentParticipation(userId, LOCAL_DATE)).thenReturn(null);

        ResponseEntity<ApiResponse<UserTournament>> responseEntity = userController.incrementUserLevel(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User level incremented successfully");
        assertThat(responseEntity.getBody().getData().getUser()).isEqualTo(user);
        assertThat(user.getLevel()).isEqualTo(11);
        assertThat(user.getCoins()).isEqualTo(525);

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).updateUser(any(User.class));
        verify(tournamentService, times(1)).getActiveTournamentParticipation(userId, LOCAL_DATE);
        verify(tournamentService, times(0)).updateUserTournamentScore(any(UserTournament.class), anyInt());
    }

    /**
     * Test for successfully entering a tournament.
     * Verifies that the user is added to the tournament and the correct leaderboard is returned.
     */
    @Test
    public void testEnterTournament_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(20);
        user.setCoins(2000);
        user.setCountry("Turkey");
        user.setUsername("testuser");

        Tournament tournament = new Tournament();
        tournament.setId(1L);

        List<TournamentCompetitorScoreDTO> leaderboard = new ArrayList<>();
        TournamentCompetitorScoreDTO dto = new TournamentCompetitorScoreDTO();
        dto.setUserId(userId);
        dto.setUsername("testuser");
        dto.setCountry("Turkey");
        dto.setScore(100);
        leaderboard.add(dto);

        when(userService.getUserById(userId)).thenReturn(user);
        when(tournamentService.findEligibleTournament(user, LOCAL_DATE)).thenReturn(tournament);
        when(tournamentService.addUserToTournament(user, tournament)).thenReturn(leaderboard);

        ResponseEntity<ApiResponse<List<TournamentCompetitorScoreDTO>>> responseEntity = userController.enterTournament(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User entered the tournament successfully");
        assertThat(responseEntity.getBody().getData()).isEqualTo(leaderboard);

        verify(userService, times(1)).getUserById(userId);
        verify(tournamentService, times(1)).findEligibleTournament(user, LOCAL_DATE);
        verify(tournamentService, times(1)).addUserToTournament(user, tournament);
    }
    /**
     * Test for entering a tournament when the user is not found.
     * Expects a ResourceNotFoundException with the appropriate error message.
     */
    @Test
    public void testEnterTournament_UserNotFound() {
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(null);

        try {
            userController.enterTournament(userId);
        } catch (ResourceNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("User not found with id: " + userId);
        }

        verify(userService, times(1)).getUserById(userId);
    }
    /**
     * Test for entering a tournament when the user is not eligible.
     * Expects a UserNotEligibleException with the appropriate error message.
     */
    @Test
    public void testEnterTournament_UserNotEligible() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(10);
        user.setCoins(2000);

        when(userService.getUserById(userId)).thenReturn(user);

        try {
            userController.enterTournament(userId);
        } catch (UserNotEligibleException e) {
            assertThat(e.getMessage()).isEqualTo("User does not have enough levels to enter the tournament.");
        }

        verify(userService, times(1)).getUserById(userId);
    }
    /**
     * Test for entering a tournament when the user has insufficient coins.
     * Expects an InsufficientCoinsException with the appropriate error message.
     */
    @Test
    public void testEnterTournament_InsufficientCoins() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(20);
        user.setCoins(500);

        when(userService.getUserById(userId)).thenReturn(user);

        try {
            userController.enterTournament(userId);
        } catch (InsufficientCoinsException e) {
            assertThat(e.getMessage()).isEqualTo("User does not have enough coins to enter the tournament.");
        }

        verify(userService, times(1)).getUserById(userId);
    }
    /**
     * Test for entering a tournament when the user has unclaimed tournaments.
     * Expects an UnclaimedTournamentException with the appropriate error message.
     */
    @Test
    public void testEnterTournament_HasUnclaimedTournaments() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(20);
        user.setCoins(2000);

        when(userService.getUserById(userId)).thenReturn(user);
        when(tournamentService.hasUnclaimedTournaments(userId)).thenReturn(true);

        try {
            userController.enterTournament(userId);
        } catch (UnclaimedTournamentException e) {
            assertThat(e.getMessage()).isEqualTo("User has unclaimed tournaments.");
        }

        verify(userService, times(1)).getUserById(userId);
        verify(tournamentService, times(1)).hasUnclaimedTournaments(userId);
    }
    /**
     * Test for entering a tournament outside of tournament hours.
     * Expects a NoTournamentAtThisHourException with the appropriate error message.
     */
    @Test
    public void testEnterTournament_NoTournamentAtThisHour() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(20);
        user.setCoins(2000);

        when(userService.getUserById(userId)).thenReturn(user);

        fixedClock = Clock.fixed(
            LOCAL_DATE.atTime(OUT_TOURNAMENT_TIME).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        try {
            userController.enterTournament(userId);
        } catch (NoTournamentAtThisHourException e) {
            assertThat(e.getMessage()).isEqualTo("No tournament available at this hour.");
        }

        verify(userService, times(1)).getUserById(userId);
    }
    /**
     * Test for entering a tournament when the user is already participating in a tournament on the current date.
     * Expects an AlreadyInTournamentException with the appropriate error message.
     */
    @Test
    public void testEnterTournament_AlreadyInTournament() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(20);
        user.setCoins(2000);

        when(userService.getUserById(userId)).thenReturn(user);
        when(tournamentService.isUserParticipating(user, LOCAL_DATE)).thenReturn(true);

        try {
            userController.enterTournament(userId);
        } catch (AlreadyInTournamentException e) {
            assertThat(e.getMessage()).isEqualTo("User is already participating in a tournament on this date.");
        }

        verify(userService, times(1)).getUserById(userId);
        verify(tournamentService, times(1)).isUserParticipating(user, LOCAL_DATE);
    }
    /**
     * Test for creating a new tournament when no eligible tournament is found.
     * Verifies that a new tournament is created and the correct leaderboard is returned.
     */
    @Test
    public void testEnterTournament_CreateNewTournament() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(20);
        user.setCoins(2000);

        Tournament tournament = new Tournament();
        tournament.setId(1L);

        List<TournamentCompetitorScoreDTO> leaderboard = new ArrayList<>();
        TournamentCompetitorScoreDTO dto = new TournamentCompetitorScoreDTO();
        dto.setUserId(userId);
        dto.setUsername("testuser");
        dto.setCountry("Turkey");
        dto.setScore(100);
        leaderboard.add(dto);

        when(userService.getUserById(userId)).thenReturn(user);
        when(tournamentService.findEligibleTournament(user, LOCAL_DATE)).thenReturn(null);
        when(tournamentService.createTournament(LOCAL_DATE)).thenReturn(tournament);
        when(tournamentService.addUserToTournament(user, tournament)).thenReturn(leaderboard);

        ResponseEntity<ApiResponse<List<TournamentCompetitorScoreDTO>>> responseEntity = userController.enterTournament(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User entered the tournament successfully");
        assertThat(responseEntity.getBody().getData()).isEqualTo(leaderboard);

        verify(userService, times(1)).getUserById(userId);
        verify(tournamentService, times(1)).findEligibleTournament(user, LOCAL_DATE);
        verify(tournamentService, times(1)).createTournament(LOCAL_DATE);
        verify(tournamentService, times(1)).addUserToTournament(user, tournament);
    }
    /**
     * Test for claimTournamentPrize when the prize is successfully claimed.
     * Verifies that the user's coins can be updated and the prize is marked as claimed.
     */
    @Test
    public void testClaimTournamentPrize_Success() {
        Long userId = 1L;
        Long tournamentId = 1L;
        User user = new User();
        user.setId(userId);
        user.setCoins(500);
        
    Tournament tournament = new Tournament();
    tournament.setId(tournamentId);
    tournament.setDate(LOCAL_DATE.minusDays(1));
    
    UserTournament userTournament = new UserTournament(user, tournament, false, 0);

    List<TournamentCompetitorScoreDTO> competitors = Arrays.asList(
        new TournamentCompetitorScoreDTO(1L, "testuser", "Turkey", 100),
        new TournamentCompetitorScoreDTO(2L, "user2", "United States", 90),
        new TournamentCompetitorScoreDTO(3L, "testuser1", "United Kingdom", 80),
        new TournamentCompetitorScoreDTO(4L, "testuser2", "Germany", 80),
        new TournamentCompetitorScoreDTO(5L, "testuser3", "France", 80)
    );

    when(userService.getUserById(userId)).thenReturn(user);
    when(tournamentService.getUserTournament(userId, tournamentId)).thenReturn(userTournament);
    when(tournamentService.getCompetitorsByTournamentId(tournamentId)).thenReturn(competitors);

    when(tournamentService.claimTournamentPrize(userId, tournamentId, 10000)).thenReturn(userTournament);

    ResponseEntity<ApiResponse<UserTournament>> responseEntity = userController.claimTournamentPrize(userId, tournamentId);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody().getMessage()).isEqualTo("Tournament prize claimed successfully");
    assertThat(responseEntity.getBody().getData()).isEqualTo(userTournament);

    verify(userService, times(1)).getUserById(userId);
    verify(tournamentService, times(1)).getUserTournament(userId, tournamentId);
    verify(tournamentService, times(1)).getCompetitorsByTournamentId(tournamentId);
    verify(tournamentService, times(1)).claimTournamentPrize(userId, tournamentId, 10000);
}

    /**
     * Test for claimTournamentPrize when the user is not part of the tournament.
     * Verifies that a ResourceNotFoundException is thrown.
     */
    @Test
    public void testClaimTournamentPrize_UserNotPartOfTournament() {
        Long userId = 1L;
        Long tournamentId = 1L;
    
        when(userService.getUserById(userId)).thenReturn(new User());
        when(tournamentService.getUserTournament(userId, tournamentId)).thenReturn(null);
    
        try {
            userController.claimTournamentPrize(userId, tournamentId);
        } catch (ResourceNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("User is not part of this tournament.");
        }
    
        verify(userService, times(1)).getUserById(userId);
        verify(tournamentService, times(1)).getUserTournament(userId, tournamentId);
    }
    
    /**
     * Test for claimTournamentPrize when the prize is already claimed.
     * Verifies that a PrizeAlreadyClaimedException is thrown.
     */
    @Test
    public void testClaimTournamentPrize_PrizeAlreadyClaimed() {
        Long userId = 1L;
        Long tournamentId = 1L;
        User user = new User();
        Tournament tournament = new Tournament();
        UserTournament userTournament = new UserTournament(user, tournament, true, 0);
    
        when(userService.getUserById(userId)).thenReturn(user);
        when(tournamentService.getUserTournament(userId, tournamentId)).thenReturn(userTournament);
    
        try {
            userController.claimTournamentPrize(userId, tournamentId);
        } catch (PrizeAlreadyClaimedException e) {
            assertThat(e.getMessage()).isEqualTo("Prize already claimed for this tournament.");
        }
    
        verify(userService, times(1)).getUserById(userId);
        verify(tournamentService, times(1)).getUserTournament(userId, tournamentId);
    }
    
    /**
     * Test for claimTournamentPrize when there are not enough competitors.
     * Verifies that a NotEnoughCompetitorsException is thrown.
     */
    @Test
    public void testClaimTournamentPrize_NotEnoughCompetitors() {
        Long userId = 1L;
        Long tournamentId = 1L;
        User user = new User();
        Tournament tournament = new Tournament();
        UserTournament userTournament = new UserTournament(user, tournament, false, 0);
    
        List<TournamentCompetitorScoreDTO> competitors = Arrays.asList(
            new TournamentCompetitorScoreDTO(1L, "user1", "Turkey", 100),
            new TournamentCompetitorScoreDTO(2L, "user2", "USA", 90)
        );
    
        when(userService.getUserById(userId)).thenReturn(user);
        when(tournamentService.getUserTournament(userId, tournamentId)).thenReturn(userTournament);
        when(tournamentService.getCompetitorsByTournamentId(tournamentId)).thenReturn(competitors);
    
        try {
            userController.claimTournamentPrize(userId, tournamentId);
        } catch (NotEnoughCompetitorsException e) {
            assertThat(e.getMessage()).isEqualTo("Not enough competitors in the tournament.");
        }
    
        verify(userService, times(1)).getUserById(userId);
        verify(tournamentService, times(1)).getUserTournament(userId, tournamentId);
        verify(tournamentService, times(1)).getCompetitorsByTournamentId(tournamentId);
    }
    
    /**
     * Test for claimTournamentPrize when the tournament has not ended.
     * Verifies that a TournamentNotEndedException is thrown.
     */
    @Test
    public void testClaimTournamentPrize_TournamentNotEnded() {
        Long userId = 1L;
        Long tournamentId = 1L;
        User user = new User();
        Tournament tournament = new Tournament();
        tournament.setDate(LOCAL_DATE);
        UserTournament userTournament = new UserTournament(user, tournament, false, 0);
    
        List<TournamentCompetitorScoreDTO> competitors = Arrays.asList(
            new TournamentCompetitorScoreDTO(userId, "testuser", "Turkey", 100),
            new TournamentCompetitorScoreDTO(2L, "user2", "United States", 90),
            new TournamentCompetitorScoreDTO(3L, "testuser1", "United Kingdom", 80),
            new TournamentCompetitorScoreDTO(4L, "testuser2", "Germany", 80),
            new TournamentCompetitorScoreDTO(5L, "testuser3", "France", 80)
        );
    
        when(userService.getUserById(userId)).thenReturn(user);
        when(tournamentService.getUserTournament(userId, tournamentId)).thenReturn(userTournament);
        when(tournamentService.getCompetitorsByTournamentId(tournamentId)).thenReturn(competitors);
    
        fixedClock = Clock.fixed(LOCAL_DATE.atTime(IN_TOURNAMENT_TIME).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    
        try {
            userController.claimTournamentPrize(userId, tournamentId);
        } catch (TournamentNotEndedException e) {
            assertThat(e.getMessage()).isEqualTo("Tournament has not ended yet.");
        }
    
        verify(userService, times(1)).getUserById(userId);
        verify(tournamentService, times(1)).getUserTournament(userId, tournamentId);
        verify(tournamentService, times(1)).getCompetitorsByTournamentId(tournamentId);
    }
    
    /**
     * Test for claimTournamentPrize when the user rank is not found.
     * Verifies that a ResourceNotFoundException is thrown.
     */
    @Test
    public void testClaimTournamentPrize_UserRankNotFound() {
        Long userId = 1L;
        Long tournamentId = 1L;
        User user = new User();
        Tournament tournament = new Tournament();
        tournament.setDate(LOCAL_DATE);
        UserTournament userTournament = new UserTournament(user, tournament, false, 0);
    
        List<TournamentCompetitorScoreDTO> competitors = Arrays.asList(
            new TournamentCompetitorScoreDTO(2L, "user2", "United States", 90),
            new TournamentCompetitorScoreDTO(3L, "testuser1", "United Kingdom", 80),
            new TournamentCompetitorScoreDTO(4L, "testuser2", "Germany", 80),
            new TournamentCompetitorScoreDTO(5L, "testuser3", "France", 80),
            new TournamentCompetitorScoreDTO(6L, "testuser3", "Turkey", 80)

        );
    
        when(userService.getUserById(userId)).thenReturn(user);
        when(tournamentService.getUserTournament(userId, tournamentId)).thenReturn(userTournament);
        when(tournamentService.getCompetitorsByTournamentId(tournamentId)).thenReturn(competitors);
        fixedClock = Clock.fixed(
            LOCAL_DATE.atTime(OUT_TOURNAMENT_TIME).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        try {
            userController.claimTournamentPrize(userId, tournamentId);
        } catch (ResourceNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("User rank not found in the tournament.");
        }
    
        verify(userService, times(1)).getUserById(userId);
        verify(tournamentService, times(1)).getUserTournament(userId, tournamentId);
        verify(tournamentService, times(1)).getCompetitorsByTournamentId(tournamentId);
    }
    
}
