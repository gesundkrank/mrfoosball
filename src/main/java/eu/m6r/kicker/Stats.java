package eu.m6r.kicker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.m6r.kicker.models.Match;
import eu.m6r.kicker.models.Team;
import eu.m6r.kicker.models.TeamStat;
import eu.m6r.kicker.models.Tournament;

public class Stats {

    private final Controller controller;

    public Stats() throws IOException {
        this.controller = Controller.getInstance();
    }

    public List<TeamStat> calcTeamStats(final String channelId) {
        final Map<Team, TeamStat> stats = new HashMap<>();
        for (final Tournament tournament : controller.getTournaments(channelId)) {
            final Team teamA = tournament.teamA;
            final TeamStat teamStatA = stats.getOrDefault(teamA, new TeamStat(teamA));
            final Team teamB = tournament.teamB;
            final TeamStat teamStatB = stats.getOrDefault(teamB, new TeamStat(teamB));

            int matchesWon = 0;
            for (final Match match : tournament.matches) {
                final int goalsTeamA = match.teamA;
                final int goalsTeamB = match.teamB;
                teamStatA.goalsScored += goalsTeamA;
                teamStatA.goalsReceived += goalsTeamB;
                teamStatB.goalsScored += goalsTeamB;
                teamStatB.goalsReceived += goalsTeamA;

                teamStatA.matchesPlayed++;
                teamStatB.matchesPlayed++;

                if (goalsTeamA > goalsTeamB) {
                    teamStatA.matchesWon++;
                    teamStatB.matchesLost++;
                    matchesWon++;
                } else if (goalsTeamB > goalsTeamA) {
                    teamStatB.matchesWon++;
                    teamStatA.matchesLost++;
                    matchesWon--;
                }
            }

            teamStatA.tournamentsPlayed++;
            teamStatB.tournamentsPlayed++;
            if (matchesWon > 0) {
                teamStatA.tournamentsWon++;
                teamStatB.tournamentsLost++;
            } else if (matchesWon < 0) {
                teamStatB.tournamentsWon++;
                teamStatA.tournamentsLost++;
            }

            stats.put(teamA, teamStatA);
            stats.put(teamB, teamStatB);
        }

        final List<TeamStat> teamStats = new ArrayList<>(stats.values());
        Collections.sort(teamStats);

        return teamStats;
    }

}
