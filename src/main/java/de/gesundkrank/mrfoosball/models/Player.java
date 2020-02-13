/*
 * This file is part of MrFoosball (https://github.com/gesundkrank/mrfoosball).
 * Copyright (c) 2020 Jan Graßegger.
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

package de.gesundkrank.mrfoosball.models;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
        @NamedQuery(
                name = "get_players_ordered_by_skill",
                query = "from Player order by (trueSkillMean - 3 * trueSkillStandardDeviation) desc"
        )
})
@Entity
@Table
public class Player extends TrueSkillColumns implements Comparable<Player>, Serializable {

    @Id
    public String id;
    public String name;
    public String avatarImage;

    public Player() {
    }

    public Player(final String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Player && ((Player) obj).id.equals(id);
    }

    @Override
    public int compareTo(Player o) {
        return id.compareTo(o.id);
    }
}
