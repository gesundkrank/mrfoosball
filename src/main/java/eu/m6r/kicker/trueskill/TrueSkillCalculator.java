package eu.m6r.kicker.trueskill;

import de.gesundkrank.jskills.GameInfo;
import de.gesundkrank.jskills.IPlayer;
import de.gesundkrank.jskills.ITeam;
import de.gesundkrank.jskills.Player;
import de.gesundkrank.jskills.Rating;
import de.gesundkrank.jskills.SkillCalculator;
import de.gesundkrank.jskills.Team;
import de.gesundkrank.jskills.trueskill.TwoTeamTrueSkillCalculator;

import eu.m6r.kicker.Store;
import eu.m6r.kicker.models.Match;
import eu.m6r.kicker.models.Tournament;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

            eu.m6r.kicker.models.Player playerA1 = store.getPlayer(tournament.teamA.player1);
            eu.m6r.kicker.models.Player playerA2 = store.getPlayer(tournament.teamA.player2);
            eu.m6r.kicker.models.Player playerB1 = store.getPlayer(tournament.teamB.player1);
            eu.m6r.kicker.models.Player playerB2 = store.getPlayer(tournament.teamB.player2);

            double skillA1 = playerSkill(tournament.teamA.player1);
            double skillA2 = playerSkill(tournament.teamA.player2);
            double skillB1 = playerSkill(tournament.teamB.player1);
            double skillB2 = playerSkill(tournament.teamB.player2);

            final List<ITeam> teams = new ArrayList<>();

            final IPlayer iPlayerA1 = new Player<>(playerA1.id);
            final IPlayer iPlayerA2 = new Player<>(playerA2.id);
            final ITeam teamA = toTeam(iPlayerA1, playerA1, iPlayerA2, playerA2);
            teams.add(teamA);

            final IPlayer iPlayerB1 = new Player<>(playerB1.id);
            final IPlayer iPlayerB2 = new Player<>(playerB2.id);
            final ITeam teamB = toTeam(iPlayerB1, playerB1, iPlayerB2, playerB2);
            teams.add(teamB);

            final Map<IPlayer, Rating> newRatings = skillCalculator
                    .calculateNewRatings(gameInfo, teams,
                                         rankTeamA(tournament),
                                         rankTeamB(tournament));

            final Rating newRatingA1 = newRatings.get(iPlayerA1);
            playerA1.trueSkillMean = newRatingA1.getMean();
            playerA1.trueSkillStandardDeviation = newRatingA1.getStandardDeviation();

            final Rating newRatingA2 = newRatings.get(iPlayerA2);
            playerA2.trueSkillMean = newRatingA2.getMean();
            playerA2.trueSkillStandardDeviation = newRatingA2.getStandardDeviation();

            final Rating newRatingB1 = newRatings.get(iPlayerB1);
            playerB1.trueSkillMean = newRatingB1.getMean();
            playerB1.trueSkillStandardDeviation = newRatingB1.getStandardDeviation();

            final Rating newRatingB2 = newRatings.get(iPlayerB2);
            playerB2.trueSkillMean = newRatingB2.getMean();
            playerB2.trueSkillStandardDeviation = newRatingB2.getStandardDeviation();

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

    public double playerSkill(final eu.m6r.kicker.models.Player player) {
        final Rating rating = new Rating(player.trueSkillMean, player.trueSkillStandardDeviation);
        return rating.getConservativeRating();
    }

    private ITeam toTeam(final IPlayer iPlayer1,
                         final eu.m6r.kicker.models.Player player1,
                         final IPlayer iPlayer2,
                         final eu.m6r.kicker.models.Player player2) {
        final Team team = new Team();
        team.addPlayer(iPlayer1, new Rating(player1.trueSkillMean,
                                            player1.trueSkillStandardDeviation));
        team.addPlayer(iPlayer2, new Rating(player2.trueSkillMean,
                                            player2.trueSkillStandardDeviation));
        return team;
    }

    private int rankTeamA(final Tournament tournament) {
        int winsTeamA = 0;
        for (final Match match : tournament.matches) {
            if (match.teamA >= match.teamB) {
                winsTeamA += 1;
            }
        }

        return winsTeamA > (tournament.bestOfN - 1) / 2 ? 1 : 2;
    }

    private int rankTeamB(final Tournament tournament) {
        int winsTeamA = 0;
        for (final Match match : tournament.matches) {
            if (match.teamA >= match.teamB) {
                winsTeamA += 1;
            }
        }

        return winsTeamA > (tournament.bestOfN - 1) / 2 ? 2 : 1;
    }

    public List<eu.m6r.kicker.models.Player> getBestMatch(
            final List<eu.m6r.kicker.models.Player> playerList
    ) {
        final eu.m6r.kicker.models.Player player1 = playerList.get(0);
        final eu.m6r.kicker.models.Player player2 = playerList.get(1);
        final eu.m6r.kicker.models.Player player3 = playerList.get(2);
        final eu.m6r.kicker.models.Player player4 = playerList.get(3);
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
