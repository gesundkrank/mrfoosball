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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
        @NamedQuery(
                name = "get_channel",
                query = "FROM Channel WHERE id = :id"
        ),
        @NamedQuery(
                name = "get_channel_by_slack_id",
                query = "FROM Channel WHERE slackId = :slackId"
        )
})
@Entity
@Table
public class Channel {

    @Id
    public String id;
    public String slackId;
    public String name;

    public Channel() {

    }

    public Channel(final String id, final String slackId, final String name) {
        this.id = id;
        this.slackId = slackId;
        this.name = name;
    }
}
