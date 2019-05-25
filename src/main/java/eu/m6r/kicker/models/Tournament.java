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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@NamedQueries({
        @NamedQuery(
                name = "get_tournament",
                query = "FROM Tournament WHERE id = :id"
        ),
        @NamedQuery(
                name = "get_tournaments",
                query = "FROM Tournament WHERE channel = :channel"
        ),
        @NamedQuery(
                name = "get_tournaments_with_state",
                query = "FROM Tournament WHERE channel = :channel "
                        + "AND state = :state ORDER BY id DESC"
        )
})
@Entity
@Table
public class Tournament implements Serializable {

    public Tournament() {
    }

    public Tournament(final int bestOfN, final Team teamA, final Team teamB,
                      final Channel channel) {
        this.bestOfN = bestOfN;
        this.teamA = teamA;
        this.teamB = teamB;
        this.channel = channel;
    }

    public Tournament(int bestOfN, Date date, Team teamA, Team teamB, State state,
                      List<Match> matches, Double teamAPlayer1SkillChange,
                      Double teamAPlayer2SkillChange, Double teamBPlayer1SkillChange,
                      Double teamBPlayer2SkillChange, Double teamASkillChange,
                      Double teamBSkillChange, Channel channel) {
        this.bestOfN = bestOfN;
        this.date = date;
        this.teamA = teamA;
        this.teamB = teamB;
        this.state = state;
        this.matches = matches;
        this.teamAPlayer1SkillChange = teamAPlayer1SkillChange;
        this.teamAPlayer2SkillChange = teamAPlayer2SkillChange;
        this.teamBPlayer1SkillChange = teamBPlayer1SkillChange;
        this.teamBPlayer2SkillChange = teamBPlayer2SkillChange;
        this.teamASkillChange = teamASkillChange;
        this.teamBSkillChange = teamBSkillChange;
        this.channel = channel;
    }

    @Id
    @GeneratedValue
    public int id;

    public int bestOfN;

    public Date date = new Date();

    @ManyToOne
    public Team teamA;

    @ManyToOne
    public Team teamB;

    @Enumerated(EnumType.STRING)
    public State state = State.RUNNING;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Match> matches = new ArrayList<>();

    public Double teamAPlayer1SkillChange;
    public Double teamAPlayer2SkillChange;
    public Double teamBPlayer1SkillChange;
    public Double teamBPlayer2SkillChange;
    public Double teamASkillChange;
    public Double teamBSkillChange;

    @ManyToOne
    public Channel channel;

    @Override
    public String toString() {
        return String.format("id=%d, teamA=%s, teamB=%s, state=%s, matches=%s", id, teamA,
                             teamB, state, matches);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Tournament that = (Tournament) o;
        return id == that.id
               && bestOfN == that.bestOfN
               && Objects.equals(date, that.date)
               && Objects.equals(teamA, that.teamA)
               && Objects.equals(teamB, that.teamB)
               && state == that.state
               && Objects.equals(matches, that.matches)
               && Objects.equals(teamAPlayer1SkillChange, that.teamAPlayer1SkillChange)
               && Objects.equals(teamAPlayer2SkillChange, that.teamAPlayer2SkillChange)
               && Objects.equals(teamBPlayer1SkillChange, that.teamBPlayer1SkillChange)
               && Objects.equals(teamBPlayer2SkillChange, that.teamBPlayer2SkillChange)
               && Objects.equals(teamASkillChange, that.teamASkillChange)
               && Objects.equals(teamBSkillChange, that.teamBSkillChange)
               && Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(id, bestOfN, date, teamA, teamB, state, matches, teamAPlayer1SkillChange,
                      teamAPlayer2SkillChange, teamBPlayer1SkillChange, teamBPlayer2SkillChange,
                      teamASkillChange, teamBSkillChange, channel);
    }

    public Team winner() {
        if (state == State.RUNNING) {
            return null;
        }

        final int sum = matches.stream()
                .map(match -> match.teamA > match.teamB ? 1 : -1)
                .reduce(Integer::sum).orElse(0);
        return sum > 0 ? teamA : teamB;
    }

}
