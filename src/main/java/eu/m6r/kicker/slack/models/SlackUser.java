package eu.m6r.kicker.slack.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackUser {
    public boolean ok;
    public String error;
    public String warning;

    public User user;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        public String id;
        public String name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SlackUser && user.id.equals(((SlackUser) obj).user.id);
    }
}
