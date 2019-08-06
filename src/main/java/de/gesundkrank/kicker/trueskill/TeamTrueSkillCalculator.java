/*
 * This file is part of kicker (https://github.com/mbrtargeting/kicker).
 * Copyright (c) 2019 Jan Graßegger.
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

package de.gesundkrank.kicker.trueskill;

import java.util.ArrayList;
import java.util.List;

import de.gesundkrank.jskills.IPlayer;
import de.gesundkrank.jskills.ITeam;
import de.gesundkrank.jskills.Player;
import de.gesundkrank.jskills.Rating;
import de.gesundkrank.jskills.Team;

import de.gesundkrank.kicker.models.Tournament;
import de.gesundkrank.kicker.store.Store;

public class TeamTrueSkillCalculator extends TrueSkillCalculator {

    @Override
    public Tournament updateRatings(Tournament tournament) {
        try (final var store = new Store()) {
            final var teamA = store.getTeam(tournament.teamA.player1, tournament.teamA.player2);
            final var teamB = store.getTeam(tournament.teamB.player1, tournament.teamB.player2);

            final double teamSkillA = teamSkill(teamA);
            final double teamSkillB = teamSkill(teamB);

            final List<ITeam> teams = new ArrayList<>();

            final IPlayer iTeamA = new Player<>(teamA.toString());
            final IPlayer iTeamB = new Player<>(teamB.toString());

            teams.add(toTeam(iTeamA, teamA));
            teams.add(toTeam(iTeamB, teamB));

            final var newRatings = skillCalculator
                    .calculateNewRatings(gameInfo, teams,
                                         rankTeamA(tournament),
                                         rankTeamB(tournament));

            var rating = newRatings.get(iTeamA);
            teamA.updateRating(rating);
            final var teamASkillChange = rating.getConservativeRating() - teamSkillA;

            rating = newRatings.get(iTeamB);
            teamB.updateRating(rating);
            final var teamBSkillChange = rating.getConservativeRating() - teamSkillB;

            tournament.teamA = teamA;
            tournament.teamB = teamB;

            tournament.teamASkillChange = teamASkillChange;
            tournament.teamBSkillChange = teamBSkillChange;

            return tournament;
        }

    }

    private double teamSkill(final de.gesundkrank.kicker.models.Team team) {
        return getRating(team.trueSkillMean, team.trueSkillStandardDeviation);
    }

    private static double getRating(final Double trueSkillMean,
                                    final Double trueSkillStandardDeviation) {
        final var rating = new Rating(trueSkillMean, trueSkillStandardDeviation);
        return rating.getConservativeRating();
    }

    private ITeam toTeam(final IPlayer trueSkillTeam,
                         final de.gesundkrank.kicker.models.Team team) {
        final var team1 = new Team();
        team1.addPlayer(trueSkillTeam,
                        new Rating(team.trueSkillMean, team.trueSkillStandardDeviation));
        return team1;
    }
}
