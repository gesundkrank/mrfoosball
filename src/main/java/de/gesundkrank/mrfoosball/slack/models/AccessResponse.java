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

package de.gesundkrank.mrfoosball.slack.models;

import javax.xml.bind.annotation.XmlAttribute;

public class AccessResponse {

    public boolean ok;
    public String error;

    @XmlAttribute(name = "access_token")
    public String accessToken;

    @XmlAttribute(name = "token_type")
    public String tokenType;

    public String scope;

    public Team team;

    @XmlAttribute(name = "app_id")
    public String appId;

    @XmlAttribute(name = "bot_user_id")
    public String botUserId;

    public static class Team {

        public String name;
        public String id;

        @Override
        public String toString() {
            return "Team{"
                   + "name='" + name + '\''
                   + ", id='" + id + '\''
                   + '}';
        }
    }

    @Override
    public String toString() {
        return "AccessResponse{"
               + "ok=" + ok
               + ", error='" + error + '\''
               + ", accessToken='" + accessToken + '\''
               + ", tokenType='" + tokenType + '\''
               + ", scope='" + scope + '\''
               + ", team=" + team
               + ", appId='" + appId + '\''
               + '}';
    }
}
