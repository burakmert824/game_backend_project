package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.entity.Tournament;
import com.dreamgames.backendengineeringcasestudy.entity.User;
import com.dreamgames.backendengineeringcasestudy.entity.UserTournament;
import com.dreamgames.backendengineeringcasestudy.controller.exception.ResourceNotFoundException;
import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserTournamentRepository;
import com.dreamgames.backendengineeringcasestudy.dto.CountryLeaderboardDTO;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class TournamentService {

    @Autowired
    private Clock clock;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTournamentRepository userTournamentRepository;

    public Tournament findEligibleTournament(User user, LocalDate currentDate) {
        List<Tournament> tournaments = tournamentRepository.findEligibleTournaments(user.getCountry(), currentDate);
        for (Tournament tournament : tournaments) {
            int competitorCount = userTournamentRepository.countByTournamentId(tournament.getId());
            if (competitorCount < 5) {
                return tournament;
            }
        }
        return null;
    }

    @Transactional
    public Tournament createTournament(LocalDate currentDate) {
        Tournament tournament = new Tournament();
        tournament.setDate(currentDate);
        return tournamentRepository.save(tournament);
    }

    @Transactional
    public List<TournamentCompetitorScoreDTO> addUserToTournament(User user, Tournament tournament) {

        List<TournamentCompetitorScoreDTO> competitors = getCompetitorsByTournamentId(tournament.getId());
    
        // Create and save UserTournament entry
        UserTournament userTournament = new UserTournament(user, tournament, false, 0);
        userTournamentRepository.save(userTournament);
    
        // Deduct coins from the user and save
        user.setCoins(user.getCoins() - 1000);
        userRepository.save(user);
    
        // Add the new competitor to the list
        TournamentCompetitorScoreDTO newCompetitor = new TournamentCompetitorScoreDTO(user.getId(), user.getUsername(), user.getCountry(), userTournament.getScore());
        competitors.add(newCompetitor);
    
        // If this is the 5th competitor, mark the tournament as started and save
        if (competitors.size() == 5) {
            tournament.setIsStarted(true);
            tournamentRepository.save(tournament);
        }
    
        return competitors;
    }

    
    public boolean isUserParticipating(User user, LocalDate currentDate) {
        return userTournamentRepository.existsByUserIdAndTournamentDate(user.getId(), currentDate);
    }


    public List<TournamentCompetitorScoreDTO> getCompetitorsByTournamentId(Long tournamentId) {
        return userTournamentRepository.findCompetitorsByTournamentIdOrderByScoreDesc(tournamentId);
    }


    public UserTournament getActiveTournamentParticipation(Long userId, LocalDate date) {
        return userTournamentRepository.findActiveTournamentParticipation(userId, date);
    }

    @Transactional
    public UserTournament updateUserTournamentScore(UserTournament userTournament, int additionalScore) {
        userTournament.setScore(userTournament.getScore() + additionalScore);
        userTournamentRepository.save(userTournament);
        return userTournament;
    }

    public boolean hasUnclaimedTournaments(Long userId) {
        List<UserTournament> unclaimedTournaments = userTournamentRepository.findUnclaimedTournamentsByUserId(userId);
        LocalDate currentDate = LocalDate.now(clock);
        LocalTime currentTime = LocalTime.now(clock);

        for (UserTournament userTournament : unclaimedTournaments) {
            if (
                !userTournament.getTournament().getDate().equals(currentDate) || currentTime.isAfter(LocalTime.of(20, 0)) && userTournament.getTournament().getDate().equals(currentDate) ) {
                List<TournamentCompetitorScoreDTO> competitors = getCompetitorsByTournamentId(userTournament.getTournament().getId());
                int rank = -1;
                for (int i = 0; i < competitors.size(); i++) {
                    if (competitors.get(i).getUserId().equals(userId)) {
                        rank = i + 1; // Rank is 1-based
                        break;
                    }
                }
                if (rank > 2) {
                    userTournament.setClaimed(true);
                    userTournamentRepository.save(userTournament);
                } else {
                    return true;
                }
            } 
            else {
                return true;
            }
        }
        return false;
    }

    public UserTournament getUserTournament(Long userId, Long tournamentId) {
        return userTournamentRepository.findByUserIdAndTournamentId(userId, tournamentId).orElse(null);
    }
    
    @Transactional
    public UserTournament claimTournamentPrize(Long userId, Long tournamentId, int prize) {
        UserTournament userTournament = userTournamentRepository.findByUserIdAndTournamentId(userId, tournamentId).orElse(null);
        if (userTournament == null) {
            throw new ResourceNotFoundException("User is not part of this tournament.");
        }
    
        User user = userTournament.getUser();
        user.setCoins(user.getCoins() + prize);
        userRepository.save(user);

        userTournament.setUser(user);
        userTournament.setClaimed(true);
        userTournamentRepository.save(userTournament);
        
        return userTournament;
    }

    @Transactional(readOnly = true)
    public List<CountryLeaderboardDTO> getCountryLeaderboardByDate(LocalDate date) {
        List<Tournament> tournaments = tournamentRepository.findByDate(date);
        Map<String, Long> countryScores = new HashMap<>();

        for (Tournament tournament : tournaments) {
            List<CountryLeaderboardDTO> leaderboard = userTournamentRepository.findCountryLeaderboardByTournamentId(tournament.getId());
            for (CountryLeaderboardDTO entry : leaderboard) {
                countryScores.put(entry.getCountry(), countryScores.getOrDefault(entry.getCountry(), 0L) + entry.getTotalScore());
            }
        }

        List<CountryLeaderboardDTO> combinedLeaderboard = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countryScores.entrySet()) {
            combinedLeaderboard.add(new CountryLeaderboardDTO(entry.getKey(), entry.getValue()));
        }

        combinedLeaderboard.sort((a, b) -> (int) (b.getTotalScore() - a.getTotalScore()));
        return combinedLeaderboard;
    }

    @Transactional(readOnly = true)
    public List<CountryLeaderboardDTO> getCountryLeaderboard(Long tournamentId) {
        return userTournamentRepository.findCountryLeaderboardByTournamentId(tournamentId);
    }

}
