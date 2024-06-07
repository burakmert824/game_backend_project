package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.entity.Tournament;
import com.dreamgames.backendengineeringcasestudy.entity.User;
import com.dreamgames.backendengineeringcasestudy.entity.UserTournament;
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


}
