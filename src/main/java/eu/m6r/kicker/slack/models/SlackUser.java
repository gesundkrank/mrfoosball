package eu.m6r.kicker.slack.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
        public Profile profile;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            @JsonProperty("real_name") public String realName;
            @JsonProperty("image_192") public String image192;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SlackUser && user.id.equals(((SlackUser) obj).user.id);
    }
}
