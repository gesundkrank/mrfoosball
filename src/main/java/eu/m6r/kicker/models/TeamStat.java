package eu.m6r.kicker.models;

public class TeamStat implements Comparable<TeamStat> {

    public Team team;
    public int tournamentsWon = 0;
    public int tournamentsLost = 0;
    public int tournamentsPlayed = 0;
    public int matchesWon = 0;
    public int matchesLost = 0;
    public int matchesPlayed = 0;
    public int goalsScored = 0;
    public int goalsReceived = 0;

    public TeamStat() {

    }

    public TeamStat(final Team team) {
        this.team = team;
    }


    @Override
    public int compareTo(TeamStat otherTeam) {
        final int differenceTeam1 = this.tournamentsWon - this.tournamentsLost;
        final int differenceTeam2 = otherTeam.tournamentsWon - otherTeam.tournamentsLost;

        if (differenceTeam1 > differenceTeam2) {
            return -1;
        }

        if (differenceTeam1 < differenceTeam2) {
            return 1;
        }

        final int matchDifferenceTeam1 = this.matchesWon - this.matchesLost;
        final int matchDifferenceTeam2 = otherTeam.matchesWon - otherTeam.matchesLost;

        if (matchDifferenceTeam1 > matchDifferenceTeam2) {
            return -1;
        }

        if (matchDifferenceTeam1 < matchDifferenceTeam2) {
            return 1;
        }

        final int goalDifferenceTeam1 = this.goalsScored - this.goalsReceived;
        final int goalDifferenceTeam2 = otherTeam.goalsScored - otherTeam.goalsReceived;

        return -Integer.compare(goalDifferenceTeam1, goalDifferenceTeam2);
    }
}
