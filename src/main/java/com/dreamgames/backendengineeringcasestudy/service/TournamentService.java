package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.entity.Tournament;
import com.dreamgames.backendengineeringcasestudy.entity.User;
import com.dreamgames.backendengineeringcasestudy.entity.UserTournament;
import com.dreamgames.backendengineeringcasestudy.controller.exception.ResourceNotFoundException;
import com.dreamgames.backendengineeringcasestudy.dto.TournamentCompetitorScoreDTO;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserTournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDate;
import java.util.List;

@Service
public class TournamentService {

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
    public void addUserToTournament(User user, Tournament tournament) {
        // Deduct 1000 coins from the user
        user.setCoins(user.getCoins() - 1000);
        userRepository.save(user);

        UserTournament userTournament = new UserTournament(user, tournament, false, 0);
        userTournamentRepository.save(userTournament);

        int competitorCount = userTournamentRepository.countByTournamentId(tournament.getId());
        if (competitorCount == 5) {
            tournament.setIsStarted(true);
            tournamentRepository.save(tournament);
        }
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
    public void updateUserTournamentScore(UserTournament userTournament, int additionalScore) {
        userTournament.setScore(userTournament.getScore() + additionalScore);
        userTournamentRepository.save(userTournament);
    }

    public boolean hasUnclaimedTournaments(Long userId) {
        return userTournamentRepository.hasUnclaimedTournaments(userId);
    }

    public UserTournament getUserTournament(Long userId, Long tournamentId) {
        return userTournamentRepository.findByUserIdAndTournamentId(userId, tournamentId).orElse(null);
    }
    
    @Transactional
    public void claimTournamentPrize(Long userId, Long tournamentId, int prize) {
        UserTournament userTournament = userTournamentRepository.findByUserIdAndTournamentId(userId, tournamentId).orElse(null);
        if (userTournament == null) {
            throw new ResourceNotFoundException("User is not part of this tournament.");
        }

        User user = userTournament.getUser();
        user.setCoins(user.getCoins() + prize);
        userRepository.save(user);

        userTournament.setClaimed(true);
        userTournamentRepository.save(userTournament);
    }

}
