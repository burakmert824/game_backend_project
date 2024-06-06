package com.dreamgames.backendengineeringcasestudy.controller;


import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
import com.dreamgames.backendengineeringcasestudy.controller.exception.ResourceNotFoundException;
import com.dreamgames.backendengineeringcasestudy.entity.User;
import com.dreamgames.backendengineeringcasestudy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dreamgames.backendengineeringcasestudy.controller.exception.UserAlreadyExistsException;
import java.util.Random;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        ApiResponse<User> response = new ApiResponse<>("User retrieved successfully", user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        ApiResponse<List<User>> response = new ApiResponse<>("Users retrieved successfully", users);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updatedUser = userService.updateUser(user);
        ApiResponse<User> response = new ApiResponse<>("User updated successfully", updatedUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        ApiResponse<Object> response = new ApiResponse<>("User deleted successfully", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Custom endpoint: Find users by country
    @GetMapping("/country")
    public ResponseEntity<ApiResponse<List<User>>> findUsersByCountry(@RequestParam String country) {
        List<User> users = userService.findUsersByCountry(country);
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
     * - Saves the updated user.
     * 
     * Responses:
     * - 200 OK: User level incremented successfully.
     * - 404 Not Found: User not found.
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
        ApiResponse<User> response = new ApiResponse<>("User level incremented successfully", updatedUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
