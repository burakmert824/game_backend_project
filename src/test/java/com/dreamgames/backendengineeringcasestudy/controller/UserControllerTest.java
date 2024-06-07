package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.controller.exception.UserAlreadyExistsException;
import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
import com.dreamgames.backendengineeringcasestudy.entity.User;
import com.dreamgames.backendengineeringcasestudy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

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
}
