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

package de.gesundkrank.kicker.models;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table
@IdClass(Team.Key.class)
public class Team extends TrueSkillColumns implements Serializable {

    @Id
    @ManyToOne(cascade = CascadeType.PERSIST)
    public Player player1;

    @Id
    @ManyToOne(cascade = CascadeType.PERSIST)
    public Player player2;

    @Override
    public String toString() {
        return String.format("player1=%s, player2=%s", player1, player2);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Team) {
            final Team oTeam = (Team) obj;
            return oTeam.player1.equals(player1) && oTeam.player2.equals(player2);
        }
        return false;
    }

    public static class Key implements Serializable {

        public Player player1;
        public Player player2;
    }
}
