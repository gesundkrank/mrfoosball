package eu.m6r.kicker.models;

import javax.xml.bind.annotation.XmlAttribute;

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

    @XmlAttribute(name = "winRate")
    public float getWinRate() {
        return (float) this.tournamentsWon / this.tournamentsPlayed;
    }

    @Override
    public int compareTo(TeamStat otherTeam) {
        return -Float.compare(getWinRate(), otherTeam.getWinRate());
    }
}
