package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.controller.exception.ResourceNotFoundException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.UserAlreadyExistsException;
import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
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


import java.util.Random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private TournamentService tournamentService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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
     * Test for incrementUserLevel.
     * Verifies that the user's level is incremented by one and 25 coins are added.
     * Checks if the user is participating in any tournaments today and updates the tournament score if applicable.
     */
    @Test
    public void testIncrementUserLevel() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(10);
        user.setCoins(500);

        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.updateUser(any(User.class))).thenReturn(user);

        UserTournament userTournament = new UserTournament(user, new Tournament(), false, 0);
        when(tournamentService.getActiveTournamentParticipation(userId, LocalDate.now(ZoneOffset.UTC)))
                .thenReturn(userTournament);

        ResponseEntity<ApiResponse<User>> responseEntity = userController.incrementUserLevel(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User level incremented successfully");
        assertThat(responseEntity.getBody().getData().getLevel()).isEqualTo(11);
        assertThat(responseEntity.getBody().getData().getCoins()).isEqualTo(525);

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).updateUser(any(User.class));
        verify(tournamentService, times(1)).getActiveTournamentParticipation(userId, LocalDate.now(ZoneOffset.UTC));
        verify(tournamentService, times(1)).updateUserTournamentScore(userTournament, 1);
    }

    /**
     * Test for incrementUserLevel when user not found.
     * Verifies that a ResourceNotFoundException is thrown when the user does not exist.
     */
    @Test
    public void testIncrementUserLevelUserNotFound() {
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(null);

        try {
            userController.incrementUserLevel(userId);
        } catch (ResourceNotFoundException ex) {
            assertThat(ex.getMessage()).isEqualTo("User not found with id: " + userId);
        }

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(0)).updateUser(any(User.class));
        verify(tournamentService, times(0)).getActiveTournamentParticipation(anyLong(), any(LocalDate.class));
        verify(tournamentService, times(0)).updateUserTournamentScore(any(UserTournament.class), anyInt());
    }

    /**
     * Test for incrementUserLevel when no active tournament participation.
     * Verifies that the user's level is incremented by one and 25 coins are added.
     */
    @Test
    public void testIncrementUserLevelNoActiveTournamentParticipation() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLevel(10);
        user.setCoins(500);

        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.updateUser(any(User.class))).thenReturn(user);
        when(tournamentService.getActiveTournamentParticipation(userId, LocalDate.now(ZoneOffset.UTC)))
                .thenReturn(null);

        ResponseEntity<ApiResponse<User>> responseEntity = userController.incrementUserLevel(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("User level incremented successfully");
        assertThat(responseEntity.getBody().getData().getLevel()).isEqualTo(11);
        assertThat(responseEntity.getBody().getData().getCoins()).isEqualTo(525);

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).updateUser(any(User.class));
        verify(tournamentService, times(1)).getActiveTournamentParticipation(userId, LocalDate.now(ZoneOffset.UTC));
        verify(tournamentService, times(0)).updateUserTournamentScore(any(UserTournament.class), anyInt());
    }

    // can't test this because of the static methods
    /**
     * Test for incrementUserLevel when within tournament hours.
     * Verifies that the user's level is incremented by one and 25 coins are added.
     * Checks if the user's tournament score is updated.
     */
    // @Test
    // public void testIncrementUserLevelWithinTournamentHours() {
    //     Long userId = 1L;
    //     User user = new User();
    //     user.setId(userId);
    //     user.setLevel(10);
    //     user.setCoins(500);

    //     when(userService.getUserById(userId)).thenReturn(user);
    //     when(userService.updateUser(any(User.class))).thenReturn(user);

    //     UserTournament userTournament = new UserTournament(user, new Tournament(), false, 0);
    //     when(tournamentService.getActiveTournamentParticipation(userId, LocalDate.now(ZoneOffset.UTC)))
    //             .thenReturn(userTournament);

    //     // Mock LocalDate and LocalTime
    //     LocalDate mockDate = LocalDate.of(2024, 6, 7);
    //     LocalTime mockTime = LocalTime.of(10, 0); // within tournament hours

        
    //     try(MockedStatic<LocalDate> mylocaldate = Mockito.mockStatic(LocalDate.class)){

    //         //mylocaldate.when(() -> LocalDate.now()).thenReturn(mockDate);
    //         //mockedLocalTime.when(() -> LocalTime.now()).thenReturn(mockTime);

    //         ResponseEntity<ApiResponse<User>> responseEntity = userController.incrementUserLevel(userId);

    //         assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    //         assertThat(responseEntity.getBody().getMessage()).isEqualTo("User level incremented successfully");
    //         assertThat(responseEntity.getBody().getData().getLevel()).isEqualTo(11);
    //         assertThat(responseEntity.getBody().getData().getCoins()).isEqualTo(525);

    //         verify(userService, times(1)).getUserById(userId);
    //         verify(userService, times(1)).updateUser(any(User.class));
    //         verify(tournamentService, times(1)).getActiveTournamentParticipation(userId, LocalDate.now(ZoneOffset.UTC));
    //         verify(tournamentService, times(1)).updateUserTournamentScore(userTournament, 1);
    //     }
    // }

    // cant test this because of the static methods
    // /**
    //  * Test for incrementUserLevel when outside tournament hours.
    //  * Verifies that the user's level is incremented by one and 25 coins are added.
    //  * Ensures that the user's tournament score is not updated.
    //  */
    // @Test
    // public void testIncrementUserLevelOutsideTournamentHours() {
    //     Long userId = 1L;
    //     User user = new User();
    //     user.setId(userId);
    //     user.setLevel(10);
    //     user.setCoins(500);

    //     when(userService.getUserById(userId)).thenReturn(user);
    //     when(userService.updateUser(any(User.class))).thenReturn(user);

    //     UserTournament userTournament = new UserTournament(user, new Tournament(), false, 0);
    //     when(tournamentService.getActiveTournamentParticipation(userId, LocalDate.now(ZoneOffset.UTC)))
    //             .thenReturn(userTournament);

    //     LocalTime currentTime = LocalTime.of(22, 0); // outside tournament hours
    //     when(LocalTime.now(ZoneOffset.UTC)).thenReturn(currentTime);

    //     ResponseEntity<ApiResponse<User>> responseEntity = userController.incrementUserLevel(userId);

    //     assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    //     assertThat(responseEntity.getBody().getMessage()).isEqualTo("User level incremented successfully");
    //     assertThat(responseEntity.getBody().getData().getLevel()).isEqualTo(11);
    //     assertThat(responseEntity.getBody().getData().getCoins()).isEqualTo(525);

    //     verify(userService, times(1)).getUserById(userId);
    //     verify(userService, times(1)).updateUser(any(User.class));
    //     verify(tournamentService, times(1)).getActiveTournamentParticipation(userId, LocalDate.now(ZoneOffset.UTC));
    //     verify(tournamentService, times(0)).updateUserTournamentScore(any(UserTournament.class), anyInt());
    // }
}
