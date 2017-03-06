package eu.m6r.kicker.slack.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    public Message() {}

    public Message(String channel, String text, String user) {
        this.channel = channel;
        this.type = "message";
        this.text = text;
        this.user = user;
    }

    public String channel;
    public String type;
    public String text;
    public String user;
}
