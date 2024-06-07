package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.controller.response.ApiResponse;
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
}
