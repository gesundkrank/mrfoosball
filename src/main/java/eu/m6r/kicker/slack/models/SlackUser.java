package eu.m6r.kicker.slack.models;

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
            @XmlAttribute(name = "real_name") public String realName;
            @XmlAttribute(name = "image_192") public String image192;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SlackUser && user.id.equals(((SlackUser) obj).user.id);
    }
}
