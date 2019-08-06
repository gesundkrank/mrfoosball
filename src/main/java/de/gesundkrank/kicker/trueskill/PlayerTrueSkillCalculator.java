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

import de.gesundkrank.kicker.models.Tournament;
import de.gesundkrank.kicker.store.Store;

public class PlayerTrueSkillCalculator extends TrueSkillCalculator {

    public Tournament updateRatings(final Tournament tournament) {
        try (final Store store = new Store()) {

            final var playerA1 = store.getPlayer(tournament.teamA.player1);
            final var playerA2 = store.getPlayer(tournament.teamA.player2);
            final var playerB1 = store.getPlayer(tournament.teamB.player1);
            final var playerB2 = store.getPlayer(tournament.teamB.player2);

            final double skillA1 = playerSkill(playerA1);
            final double skillA2 = playerSkill(playerA2);
            final double skillB1 = playerSkill(playerB1);
            final double skillB2 = playerSkill(playerB2);

            final List<ITeam> teams = new ArrayList<>();

            final IPlayer iPlayerA1 = new Player<>(playerA1.id);
            final IPlayer iPlayerA2 = new Player<>(playerA2.id);
            teams.add(toTeam(iPlayerA1, playerA1, iPlayerA2, playerA2));

            final IPlayer iPlayerB1 = new Player<>(playerB1.id);
            final IPlayer iPlayerB2 = new Player<>(playerB2.id);
            teams.add(toTeam(iPlayerB1, playerB1, iPlayerB2, playerB2));

            final var newRatings = skillCalculator
                    .calculateNewRatings(gameInfo, teams,
                                         rankTeamA(tournament),
                                         rankTeamB(tournament));

            var rating = newRatings.get(iPlayerA1);
            playerA1.updateRating(rating);
            final var teamAPlayer1SkillChange = rating.getConservativeRating() - skillA1;

            rating = newRatings.get(iPlayerA2);
            playerA2.updateRating(rating);
            final var teamAPlayer2SkillChange = rating.getConservativeRating() - skillA2;

            rating = newRatings.get(iPlayerB1);
            playerB1.updateRating(rating);
            final var teamBPlayer1SkillChange = rating.getConservativeRating() - skillB1;

            rating = newRatings.get(iPlayerB2);
            playerB2.updateRating(rating);
            final var teamBPlayer2SkillChange = rating.getConservativeRating() - skillB2;

            tournament.teamA.player1 = playerA1;
            tournament.teamA.player2 = playerA2;
            tournament.teamB.player1 = playerB1;
            tournament.teamB.player2 = playerB2;

            tournament.teamAPlayer1SkillChange = teamAPlayer1SkillChange;
            tournament.teamAPlayer2SkillChange = teamAPlayer2SkillChange;
            tournament.teamBPlayer1SkillChange = teamBPlayer1SkillChange;
            tournament.teamBPlayer2SkillChange = teamBPlayer2SkillChange;

            return tournament;
        }
    }

    private double playerSkill(final de.gesundkrank.kicker.models.Player player) {
        final var rating = new Rating(player.trueSkillMean, player.trueSkillStandardDeviation);
        return rating.getConservativeRating();
    }
}
