package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
import com.dreamgames.backendengineeringcasestudy.controller.exception.ResourceNotFoundException;
import com.dreamgames.backendengineeringcasestudy.entity.User;
import com.dreamgames.backendengineeringcasestudy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
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

    // Custom endpoint: Increment user's level
    @PatchMapping("/{id}/increment-level")
    public ResponseEntity<ApiResponse<User>> incrementUserLevel(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        user.setLevel(user.getLevel() + 1);
        User updatedUser = userService.updateUser(user);
        ApiResponse<User> response = new ApiResponse<>("User level incremented successfully", updatedUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
