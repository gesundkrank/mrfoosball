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

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;

public class EventWrapper {

    @XmlAttribute(name = "team_id")
    public String teamId;

    @XmlAttribute(name = "api_app_id")
    public String apiAppId;

    @XmlAttribute(name = "authed_users")
    public List<String> authedUsers;

    @XmlAttribute(name = "event_id")
    public String eventId;

    @XmlAttribute(name = "event_time")
    public long eventTime;

    public Event event;

    @Override
    public String toString() {
        return "EventWrapper{"
               + "teamId='" + teamId + '\''
               + ", apiAppId='" + apiAppId + '\''
               + ", authedUsers=" + authedUsers
               + ", eventId='" + eventId + '\''
               + ", eventTime=" + eventTime
               + ", event=" + event
               + '}';
    }

    public static class Event {

        public String type;
        public String user;
        public String team;
        public String text;
        public String ts;
        public String channel;

        @XmlAttribute(name = "event_ts")
        public String eventTs;

        @Override
        public String toString() {
            return "Event{"
                   + "type='" + type + '\''
                   + ", user='" + user + '\''
                   + ", team='" + team + '\''
                   + ", text='" + text + '\''
                   + ", ts='" + ts + '\''
                   + ", channel='" + channel + '\''
                   + ", eventTs='" + eventTs + '\''
                   + '}';
        }
    }


}
