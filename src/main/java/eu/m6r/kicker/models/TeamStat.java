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
