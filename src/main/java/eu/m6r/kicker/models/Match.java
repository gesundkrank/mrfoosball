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

import java.util.Date;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Match {

    public Match() {
    }

    public Match(final Date date, final int teamA, final int teamB, final State state) {
        this.date = date;
        this.teamA = teamA;
        this.teamB = teamB;
        this.state = state;
    }

    @Id
    @GeneratedValue
    public int id;
    public Date date = new Date();
    public int teamA = 0;
    public int teamB = 0;

    @Enumerated(EnumType.STRING)
    public State state = State.RUNNING;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Match match = (Match) o;
        return id == match.id
               && teamA == match.teamA
               && teamB == match.teamB
               && Objects.equals(date, match.date)
               && state == match.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, teamA, teamB, state);
    }
}
