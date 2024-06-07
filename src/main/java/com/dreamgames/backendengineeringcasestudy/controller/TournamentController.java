package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
import com.dreamgames.backendengineeringcasestudy.dto.CountryLeaderboardDTO;
import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/{tournamentId}/leadboard")
    public ResponseEntity<ApiResponse<List<TournamentCompetitorScoreDTO>>> getCompetitorsByTournamentId(@PathVariable Long tournamentId) {
        List<TournamentCompetitorScoreDTO> competitors = tournamentService.getCompetitorsByTournamentId(tournamentId);
        ApiResponse<List<TournamentCompetitorScoreDTO>> response = new ApiResponse<>("Leadboard retrieved successfully", competitors);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get country leaderboard for a tournament.
     * 
     * Endpoint: GET /tournaments/{tournamentId}/country-leaderboard
     * 
     * Actions:
     * - Retrieves the total scores contributed by each user competing for their respective country.
     * - Aggregates the scores by country and sorts from highest to lowest.
     * 
     * Responses:
     * - 200 OK: Returns the country leaderboard.
     * - 404 Not Found: Tournament not found.
     * - 500 Internal Server Error: Other errors.
     * 
     * @param tournamentId The ID of the tournament.
     * @return ResponseEntity with the ApiResponse containing the leaderboard data or an error message.
     */
    @GetMapping("/{tournamentId}/country-leaderboard")
    public ResponseEntity<ApiResponse<List<CountryLeaderboardDTO>>> getCountryLeaderboard(@PathVariable Long tournamentId) {
        List<CountryLeaderboardDTO> leaderboard = tournamentService.getCountryLeaderboard(tournamentId);
        ApiResponse<List<CountryLeaderboardDTO>> response = new ApiResponse<>("Country leaderboard retrieved successfully", leaderboard);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
