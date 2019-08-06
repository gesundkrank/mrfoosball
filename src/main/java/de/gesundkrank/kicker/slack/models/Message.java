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

package de.gesundkrank.kicker.slack.models;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;

public class Message {

    public String channel;
    public String type;
    public String text;
    public String user;
    @XmlAttribute(name = "as_user")
    public boolean asUser = true;
    public List<Attachment> attachments = new ArrayList<>();

    public Message() {
    }

    public Message(final String channel, final String text, final String user) {
        this.channel = channel;
        this.type = "message";
        this.text = text;
        this.user = user;
    }

    public static class Attachment {

        public String title;
        public String text;
        public String color = "#ff0080";
        @XmlAttribute(name = "image_url")
        public String imageUrl;

        @XmlAttribute(name = "mrkdwn_in")
        public List<String> mrkdwnIn = new ArrayList<>();
        public List<Field> fields = new ArrayList<>();

        public Attachment() {
        }

        public Attachment(final String title) {
            this.title = title;
        }

        public Attachment(final String title, final String text) {
            this.title = title;
            this.text = text;
            mrkdwnIn.add("text");
            mrkdwnIn.add("fields");
        }

        public static class Field {

            public String title;
            public String value;

            @XmlAttribute(name = "short")
            public boolean isShort = false;

            public Field() {

            }

            public Field(String value) {
                this.value = value;
            }

            public Field(String title, String value) {
                this.title = title;
                this.value = value;
            }
        }
    }
}
