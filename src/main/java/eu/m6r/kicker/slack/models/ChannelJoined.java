package eu.m6r.kicker.slack.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelJoined {

    public Channel channel;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Channel {

        public String id;
        public String name;
    }


}
