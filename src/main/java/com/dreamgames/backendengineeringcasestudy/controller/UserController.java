package com.dreamgames.backendengineeringcasestudy.controller;


import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.controller.exception.AlreadyInTournamentException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.InsufficientCoinsException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.NoTournamentAtThisHourException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.NotEnoughCompetitorsException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.PrizeAlreadyClaimedException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.ResourceNotFoundException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.TournamentNotEndedException;
import com.dreamgames.backendengineeringcasestudy.entity.Tournament;
import com.dreamgames.backendengineeringcasestudy.entity.User;
import com.dreamgames.backendengineeringcasestudy.entity.UserTournament;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dreamgames.backendengineeringcasestudy.controller.exception.UserAlreadyExistsException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.UserNotEligibleException;
import com.dreamgames.backendengineeringcasestudy.controller.exception.UnclaimedTournamentException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TournamentService tournamentService;

    /**
     * Create a new user with default values.
     * 
     * Endpoint: POST /api/users
     * Request Parameter: username (required)
     * 
     * Actions:
     * - Checks if the username already exists.
     * - If yes, returns 409 Conflict with an error message.
     * - If no, creates a user with default values:
     *   - Coins: 5000
     *   - Level: 1
     *   - Country: Random from Turkey, United States, United Kingdom, France, Germany
     * 
     * Responses:
     * - 201 Created: User created successfully.
     * - 409 Conflict: User already exists.
     * - 500 Internal Server Error: Other errors.
     * 
     * Example:
     * curl -X POST "http://localhost:8080/api/users?username=johndoe"
     * 
     * @param username The username of the user to be created.
     * @return ResponseEntity with the ApiResponse containing the created user or an error message.
     * @throws UserAlreadyExistsException if the username already exists.
    */  
   @PostMapping
   public ResponseEntity<ApiResponse<User>> createUser(@RequestParam String username) {
       // Check if the user already exists
       if (userService.existsByUsername(username)) {
           throw new UserAlreadyExistsException("User already exists with username: " + username);
       }

       User user = new User();
       user.setUsername(username);
       user.setCoins(5000);
       user.setLevel(1);

       // Assign a random country
       String[] countries = {"Turkey", "United States", "United Kingdom", "France", "Germany"};
       Random random = new Random();
       String randomCountry = countries[random.nextInt(countries.length)];
       user.setCountry(randomCountry);

       User createdUser = userService.createUser(user);
       ApiResponse<User> response = new ApiResponse<>("User created successfully", createdUser);
       return new ResponseEntity<>(response, HttpStatus.CREATED);
   }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        ApiResponse<List<User>> response = new ApiResponse<>("Users retrieved successfully", users);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

   
    /**
     * Increment user's level by one and add 25 coins.
     * 
     * Endpoint: PATCH /api/users/{id}/increment-level
     * 
     * Actions:
     * - Retrieves the user by ID.
     * - Increases the user's level by one.
     * - Adds 25 coins to the user's current coins.
     * - Checks if the user is participating in any tournaments today that have started.
     * - If participating, checks if the current time is within tournament hours (00:00 to 20:00 UTC).
     * - If within hours, updates the tournament score by adding a specified amount.
     * - Saves the updated user and user-tournament.
     * 
     * Responses:
     * - 200 OK: User level incremented successfully.
     * - 404 Not Found: User not found.
     * - 400 Bad Request: Tournament not within hours.
     * - 500 Internal Server Error: Other errors.
     * 
     * Example:
     * curl -X PATCH "http://localhost:8080/api/users/1/increment-level"
     * 
     * @param id The ID of the user whose level is to be incremented.
     * @return ResponseEntity with the ApiResponse containing the updated user or an error message.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @PatchMapping("/{id}/increment-level")
    public ResponseEntity<ApiResponse<User>> incrementUserLevel(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        user.setLevel(user.getLevel() + 1);
        user.setCoins(user.getCoins() + 25);
        User updatedUser = userService.updateUser(user);

        // Check if the user is participating in any tournaments today that have started
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        UserTournament userTournament = tournamentService.getActiveTournamentParticipation(user.getId(), currentDate);
        LocalTime currentTime = LocalTime.now(ZoneOffset.UTC);
        if (userTournament != null) {
            // Check if the current time is within tournament hours
            if (currentTime.isBefore(LocalTime.of(20, 0))) {
                tournamentService.updateUserTournamentScore(userTournament, 1); // Example: add 100 points
            }
        }

        ApiResponse<User> response = new ApiResponse<>("User level incremented successfully", updatedUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
   
    /**
     * Enter a user into the current tournament.
     * 
     * Endpoint: POST /users/{id}/enter-tournament
     * 
     * Actions:
     * - Checks if the user has more than 1000 coins and 20 level.
     * - Finds a tournament that:
     *   - Has no competitors from the user's country.
     *   - Has less than 5 competitors.
     * - Deducts 1000 coins from the user.
     * - Enters the user into the tournament.
     * - If the user is the 5th competitor, sets the tournament's isStarted field to true.
     * - Checks if the current time is between 00:00 and 20:00 UTC. 
     *   If not, throws NoTournamentAtThisHourException.
     * - Checks if the user is already participating in a tournament on the current date. 
     *   If yes, throws AlreadyInTournamentException.
     * - Checks if the user has any unclaimed tournaments. 
     *   If yes, throws UnclaimedTournamentException.
     * - If no eligible tournament is found, creates a new tournament.
     * 
     * Responses:
     * - 200 OK: User entered the tournament successfully.
     * - 404 Not Found: User not found or no eligible tournament found.
     * - 400 Bad Request: User not eligible to enter the tournament.
     * - 400 Bad Request: User does not have enough coins.
     * - 400 Bad Request: No tournament at this hour.
     * - 400 Bad Request: User already in a tournament on this date.
     * - 400 Bad Request: User has unclaimed tournaments.
     * 
     * @param id The ID of the user who wants to enter the tournament.
     * @return ResponseEntity with the ApiResponse containing the tournament details or an error message.
     * @throws ResourceNotFoundException if the user or tournament is not found.
     * @throws UserNotEligibleException if the user is not eligible to enter the tournament.
     * @throws InsufficientCoinsException if the user does not have enough coins.
     * @throws NoTournamentAtThisHourException if the current time is not between 00:00 and 20:00 UTC.
     * @throws AlreadyInTournamentException if the user is already participating in a tournament on the current date.
     * @throws UnclaimedTournamentException if the user has unclaimed tournaments.
     */
    @PostMapping("/{id}/enter-tournament")
    public ResponseEntity<ApiResponse<Tournament>> enterTournament(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        // Check if the user has more than 1000 coins and 20 level
        if (user.getLevel() < 20) {
            throw new UserNotEligibleException("User does not have enough levels to enter the tournament.");
        }

        if (user.getCoins() < 1000) {
            throw new InsufficientCoinsException("User does not have enough coins to enter the tournament.");
        }

        // Check if the user has any unclaimed tournaments
        if (tournamentService.hasUnclaimedTournaments(user.getId())) {
            throw new UnclaimedTournamentException("User has unclaimed tournaments.");
        }

        // Check if the current time is between 00:00 and 20:00 UTC
        LocalTime currentTime = LocalTime.now(ZoneOffset.UTC);
        if (currentTime.isBefore(LocalTime.of(0, 0)) || currentTime.isAfter(LocalTime.of(20, 0))) {
            throw new NoTournamentAtThisHourException("No tournament available at this hour.");
        }

        // Get the current date
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        
        //todo: this can be deleted because this tournament will be shown in the unclaimed tournament list
        // Check if the user is already participating in a tournament on the current date
        if (tournamentService.isUserParticipating(user, currentDate)) {
            throw new AlreadyInTournamentException("User is already participating in a tournament on this date.");
        }

        // Find eligible tournament
        Tournament tournament = tournamentService.findEligibleTournament(user, currentDate);
        if (tournament == null) {
            // Create a new tournament if no eligible tournament is found
            tournament = tournamentService.createTournament(currentDate);
        }

        tournamentService.addUserToTournament(user, tournament);
        ApiResponse<Tournament> response = new ApiResponse<>("User entered the tournament successfully", tournament);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


         /**
     * Claim tournament prize.
     * 
     * Endpoint: POST /users/{userId}/tournaments/{tournamentId}/claim-prize
     * 
     * Actions:
     * - Checks if the tournament has at least 5 competitors.
     * - Checks the user's rank in the tournament.
     * - If the rank is 1st or 2nd, adds the prize to the user's coins.
     * - Marks the tournament as claimed.
     * 
     * Responses:
     * - 200 OK: Tournament prize claimed successfully.
     * - 404 Not Found: User or tournament not found.
     * - 400 Bad Request: Not enough competitors, prize already claimed, or tournament not ended.
     * 
     * @param userId The ID of the user claiming the prize.
     * @param tournamentId The ID of the tournament.
     * @return ResponseEntity with the ApiResponse containing the result or an error message.
     * @throws ResourceNotFoundException if the user or tournament is not found.
     */
    @PostMapping("/{userId}/tournaments/{tournamentId}/claim-prize")
    public ResponseEntity<ApiResponse<String>> claimTournamentPrize(@PathVariable Long userId, @PathVariable Long tournamentId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        UserTournament userTournament = tournamentService.getUserTournament(userId, tournamentId);
        if (userTournament == null) {
            throw new ResourceNotFoundException("User is not part of this tournament.");
        }

        if (userTournament.isClaimed()) {
            throw new PrizeAlreadyClaimedException("Prize already claimed for this tournament.");
        }

        List<TournamentCompetitorScoreDTO> competitors = tournamentService.getCompetitorsByTournamentId(tournamentId);
        if (competitors.size() < 5) {
            throw new NotEnoughCompetitorsException("Not enough competitors in the tournament.");
        }

        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        LocalTime currentTime = LocalTime.now(ZoneOffset.UTC);
        Tournament tournament = userTournament.getTournament();
        if (tournament.getDate().equals(currentDate) && currentTime.isBefore(LocalTime.of(20, 0))) {
            throw new TournamentNotEndedException("Tournament has not ended yet.");
        }

        int rank = -1;
        for (int i = 0; i < competitors.size(); i++) {
            if (competitors.get(i).getUserId().equals(userId)) {
                rank = i + 1; // Rank is 1-based
                break;
            }
        }

        if (rank == -1) {
            throw new ResourceNotFoundException("User rank not found in the tournament.");
        }

        int prize = 0;
        if (rank == 1) {
            prize = 10000;
        } else if (rank == 2) {
            prize = 5000;
        }

        tournamentService.claimTournamentPrize(userId, tournamentId, prize);

        ApiResponse<String> response = new ApiResponse<>("Tournament prize claimed successfully", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
