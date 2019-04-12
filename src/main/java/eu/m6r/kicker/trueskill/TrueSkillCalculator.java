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

package eu.m6r.kicker.trueskill;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.gesundkrank.jskills.GameInfo;
import de.gesundkrank.jskills.IPlayer;
import de.gesundkrank.jskills.ITeam;
import de.gesundkrank.jskills.Player;
import de.gesundkrank.jskills.Rating;
import de.gesundkrank.jskills.SkillCalculator;
import de.gesundkrank.jskills.Team;
import de.gesundkrank.jskills.trueskill.TwoTeamTrueSkillCalculator;

import eu.m6r.kicker.models.PlayerQueue;
import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.store.Store;

public class TrueSkillCalculator {

    public static final double DEFAULT_INITIAL_MEAN = 25.0;
    public static final double DEFAULT_BETA = DEFAULT_INITIAL_MEAN / 6.0;
    public static final double DEFAULT_DRAW_PROBABILITY = 0.0;
    public static final double DEFAULT_DYNAMICS_FACTOR = DEFAULT_INITIAL_MEAN / 300.0;
    public static final double DEFAULT_INITIAL_STANDARD_DEVIATION = DEFAULT_INITIAL_MEAN / 3.0;

    private final SkillCalculator skillCalculator;
    private final GameInfo gameInfo;

    public TrueSkillCalculator() {
        this.skillCalculator = new TwoTeamTrueSkillCalculator();
        this.gameInfo = new GameInfo(DEFAULT_INITIAL_MEAN, DEFAULT_INITIAL_STANDARD_DEVIATION,
                                     DEFAULT_BETA, DEFAULT_DYNAMICS_FACTOR,
                                     DEFAULT_DRAW_PROBABILITY);
    }

    public Tournament updateRatings(final Tournament tournament) {
        try (final Store store = new Store()) {

            final var playerA1 = store.getPlayer(tournament.teamA.player1);
            final var playerA2 = store.getPlayer(tournament.teamA.player2);
            final var playerB1 = store.getPlayer(tournament.teamB.player1);
            final var playerB2 = store.getPlayer(tournament.teamB.player2);

            final double skillA1 = playerSkill(tournament.teamA.player1);
            final double skillA2 = playerSkill(tournament.teamA.player2);
            final double skillB1 = playerSkill(tournament.teamB.player1);
            final double skillB2 = playerSkill(tournament.teamB.player2);

            final List<ITeam> teams = new ArrayList<>();

            final IPlayer iPlayerA1 = new Player<>(playerA1.id);
            final IPlayer iPlayerA2 = new Player<>(playerA2.id);
            final var teamA = toTeam(iPlayerA1, playerA1, iPlayerA2, playerA2);
            teams.add(teamA);

            final IPlayer iPlayerB1 = new Player<>(playerB1.id);
            final IPlayer iPlayerB2 = new Player<>(playerB2.id);
            final var teamB = toTeam(iPlayerB1, playerB1, iPlayerB2, playerB2);
            teams.add(teamB);

            final var newRatings = skillCalculator
                    .calculateNewRatings(gameInfo, teams,
                                         rankTeamA(tournament),
                                         rankTeamB(tournament));

            final var newRatingA1 = newRatings.get(iPlayerA1);
            playerA1.updateRating(newRatingA1);

            final var newRatingA2 = newRatings.get(iPlayerA2);
            playerA2.updateRating(newRatings.get(iPlayerA2));

            final var newRatingB1 = newRatings.get(iPlayerB1);
            playerB1.updateRating(newRatings.get(iPlayerB1));

            final var newRatingB2 = newRatings.get(iPlayerB2);
            playerB2.updateRating(newRatings.get(iPlayerB2));

            tournament.teamA.player1 = playerA1;
            tournament.teamA.player2 = playerA2;
            tournament.teamB.player1 = playerB1;
            tournament.teamB.player2 = playerB2;

            tournament.teamAPlayer1SkillChange = newRatingA1.getConservativeRating() - skillA1;
            tournament.teamAPlayer2SkillChange = newRatingA2.getConservativeRating() - skillA2;
            tournament.teamBPlayer1SkillChange = newRatingB1.getConservativeRating() - skillB1;
            tournament.teamBPlayer2SkillChange = newRatingB2.getConservativeRating() - skillB2;

            return tournament;
        }
    }

    private double playerSkill(final eu.m6r.kicker.models.Player player) {
        final var rating = new Rating(player.trueSkillMean, player.trueSkillStandardDeviation);
        return rating.getConservativeRating();
    }

    private ITeam toTeam(final IPlayer trueSkillPlayer1,
                         final eu.m6r.kicker.models.Player player1,
                         final IPlayer trueSkillPlayer2,
                         final eu.m6r.kicker.models.Player player2) {
        final var team = new Team();
        team.addPlayer(trueSkillPlayer1, new Rating(player1.trueSkillMean,
                                                    player1.trueSkillStandardDeviation));
        team.addPlayer(trueSkillPlayer2, new Rating(player2.trueSkillMean,
                                                    player2.trueSkillStandardDeviation));
        return team;
    }

    private boolean teamAWins(final Tournament tournament) {
        int winsTeamA = 0;
        for (final var match : tournament.matches) {
            if (match.teamA >= match.teamB) {
                winsTeamA += 1;
            }
        }

        return winsTeamA > (tournament.bestOfN - 1) / 2;
    }

    private int rankTeamA(final Tournament tournament) {
        return teamAWins(tournament) ? 1 : 2;
    }

    private int rankTeamB(final Tournament tournament) {
        return teamAWins(tournament) ? 2 : 1;
    }

    public List<eu.m6r.kicker.models.Player> getBestMatch(
            final List<eu.m6r.kicker.models.Player> playerList
    ) {
        final var player1 = playerList.get(0);
        final var player2 = playerList.get(1);
        final var player3 = playerList.get(2);
        final var player4 = playerList.get(3);
        final double quality1234 = calcMatchQuality(player1, player2, player3, player4);
        final double quality1324 = calcMatchQuality(player1, player3, player2, player4);
        final double quality1423 = calcMatchQuality(player1, player4, player2, player3);

        final List<eu.m6r.kicker.models.Player> bestMatch = new LinkedList<>();
        if (quality1234 >= Math.max(quality1324, quality1423)) {
            bestMatch.add(player1);
            bestMatch.add(player2);
            bestMatch.add(player3);
            bestMatch.add(player4);
        } else if (quality1324 >= Math.max(quality1234, quality1423)) {
            bestMatch.add(player1);
            bestMatch.add(player3);
            bestMatch.add(player2);
            bestMatch.add(player4);
        } else {
            bestMatch.add(player1);
            bestMatch.add(player4);
            bestMatch.add(player2);
            bestMatch.add(player3);
        }

        return bestMatch;
    }

    private double calcMatchQuality(final eu.m6r.kicker.models.Player player1,
                                    final eu.m6r.kicker.models.Player player2,
                                    final eu.m6r.kicker.models.Player player3,
                                    final eu.m6r.kicker.models.Player player4) {
        final IPlayer iPlayer1 = new Player<>(player1.id);
        final IPlayer iPlayer2 = new Player<>(player2.id);
        final IPlayer iPlayer3 = new Player<>(player3.id);
        final IPlayer iPlayer4 = new Player<>(player4.id);

        final List<ITeam> teams = new LinkedList<>();
        teams.add(toTeam(iPlayer1, player1, iPlayer2, player2));
        teams.add(toTeam(iPlayer3, player3, iPlayer4, player4));
        return skillCalculator.calculateMatchQuality(gameInfo, teams);
    }
}
