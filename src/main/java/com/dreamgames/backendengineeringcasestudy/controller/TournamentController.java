package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.controller.exception.ResourceNotFoundException;
import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
import com.dreamgames.backendengineeringcasestudy.dto.CountryLeaderboardDTO;
import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    /**
     * Retrieve the user's ranking in a specific tournament.
     * 
     * Endpoint: GET /tournaments/{tournamentId}/user/{userId}/rank
     * 
     * This endpoint retrieves the ranking of a user in a given tournament.
     * 
     * Actions:
     * - Retrieves the tournament leaderboard based on the given tournament ID.
     * - Finds the user's position in the leaderboard.
     * - Constructs an ApiResponse object with a success message and the user's rank.
     * 
     * Responses:
     * - 200 OK: Returns the user's rank in the tournament successfully.
     * - 404 Not Found: If the user or tournament is not found.
     * - 500 Internal Server Error: If an error occurs while retrieving the rank.
     * 
     * Example:
     * curl -X GET "http://localhost:8080/tournaments/1/user/1/rank"
     * 
     * @param tournamentId The ID of the tournament.
     * @param userId The ID of the user.
     * @return ResponseEntity containing the ApiResponse with the user's rank or an error message.
     * @throws ResourceNotFoundException if the user or tournament is not found.
     */
    @GetMapping("/{tournamentId}/user/{userId}/rank")
    public ResponseEntity<ApiResponse<Integer>> getUserRankingInTournament(@PathVariable Long tournamentId, @PathVariable Long userId) {
        List<TournamentCompetitorScoreDTO> competitors = tournamentService.getCompetitorsByTournamentId(tournamentId);
        
        int rank = -1;
        for (int i = 0; i < competitors.size(); i++) {
            if (competitors.get(i).getUserId().equals(userId)) {
                rank = i + 1; // Rank is 1-based
                break;
            }
        }

        if (rank == -1) {
            throw new ResourceNotFoundException("User or tournament not found");
        }

        ApiResponse<Integer> response = new ApiResponse<>("User ranking retrieved successfully", rank);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieve the leaderboard for a specific tournament.
     * 
     * Endpoint: GET /tournaments/{tournamentId}/leadboard
     * 
     * This endpoint retrieves the leaderboard for a given tournament. The leaderboard
     * contains a list of competitors along with their scores, sorted from highest to lowest.
     * 
     * Actions:
     * - Retrieves the tournament leaderboard based on the given tournament ID.
     * - Constructs an ApiResponse object with a success message and the leaderboard data.
     * 
     * Responses:
     * - 200 OK: Returns the tournament leaderboard successfully.
     * - 404 Not Found: If the tournament is not found.
     * - 500 Internal Server Error: If an error occurs while retrieving the leaderboard.
     * 
     * Example:
     * curl -X GET "http://localhost:8080/tournaments/1/leadboard"
     * 
     * @param tournamentId The ID of the tournament whose leaderboard is to be retrieved.
     * @return ResponseEntity containing the ApiResponse with the leaderboard data or an error message.
     * @throws ResourceNotFoundException if the tournament is not found.
     */
    @GetMapping("/{tournamentId}/leadboard")
    public ResponseEntity<ApiResponse<List<TournamentCompetitorScoreDTO>>> getCompetitorsByTournamentId(@PathVariable Long tournamentId) {
        List<TournamentCompetitorScoreDTO> competitors = tournamentService.getCompetitorsByTournamentId(tournamentId);
        ApiResponse<List<TournamentCompetitorScoreDTO>> response = new ApiResponse<>("Leadboard retrieved successfully", competitors);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get country leaderboard for all tournaments on a given date.
     * 
     * Endpoint: GET /tournaments/country-leaderboard?date=YYYY-MM-DD
     * 
     * Actions:
     * - Retrieves the total scores contributed by each user competing for their respective country.
     * - Aggregates the scores by country and sorts from highest to lowest.
     * 
     * Responses:
     * - 200 OK: Returns the country leaderboard.
     * - 404 Not Found: No tournaments found on the given date.
     * - 500 Internal Server Error: Other errors.
     * 
     * @param date The date of the tournaments.
     * @return ResponseEntity with the ApiResponse containing the leaderboard data or an error message.
     */
    @GetMapping("/country-leaderboard")
    public ResponseEntity<ApiResponse<List<CountryLeaderboardDTO>>> getCountryLeaderboard(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CountryLeaderboardDTO> leaderboard = tournamentService.getCountryLeaderboardByDate(date);
        if (leaderboard.isEmpty()) {
            throw new ResourceNotFoundException("No tournaments found on date: " + date);
        }
        ApiResponse<List<CountryLeaderboardDTO>> response = new ApiResponse<>("Country leaderboard retrieved successfully", leaderboard);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
