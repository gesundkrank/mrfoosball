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
                name = "get_slack_workspace",
                query = "FROM SlackWorkspace WHERE teamId = :teamId"
        )})
@Entity
@Table
public class SlackWorkspace {

    @Id
    public String teamId;
    public String accessToken;
    public String scope;
    public String teamName;
    public String botUserId;

    public SlackWorkspace() {
    }

    public SlackWorkspace(final String teamId, final String accessToken, final String scope,
                          final String teamName, final String botUserId) {
        this.teamId = teamId;
        this.accessToken = accessToken;
        this.scope = scope;
        this.teamName = teamName;
        this.botUserId = botUserId;
    }
}
