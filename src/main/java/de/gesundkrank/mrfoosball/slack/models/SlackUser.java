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

public class SlackUser {

    public boolean ok;
    public String error;
    public String warning;

    public User user;

    public static class User {

        public String id;
        public String name;
        public Profile profile;

        public static class Profile {

            @XmlAttribute(name = "real_name")
            public String realName;
            @XmlAttribute(name = "image_192")
            public String image192;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SlackUser && user.id.equals(((SlackUser) obj).user.id);
    }
}
