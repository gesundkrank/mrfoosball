/*
 * This file is part of MrFoosball (https://github.com/gesundkrank/mrfoosball).
 * Copyright (c) 2020 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.gesundkrank.mrfoosball;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gesundkrank.mrfoosball.models.Match;
import de.gesundkrank.mrfoosball.models.Team;
import de.gesundkrank.mrfoosball.models.TeamStat;
import de.gesundkrank.mrfoosball.models.Tournament;

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
