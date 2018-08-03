package eu.m6r.kicker.slack.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

    public String channel;
    public String type;
    public String text;
    public String user;
    public boolean as_user;
    public List<Attachment> attachments = new ArrayList<>();

    public Message() {
    }

    public Message(final String channel, final String text, final String user) {
        this.channel = channel;
        this.type = "message";
        this.text = text;
        this.user = user;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {

        public String title;
        public String text;
        public String color = "#ff0080";
        public String image_url;

        public List<String> mrkdwn_in = new ArrayList<>();
        public List<Field> fields = new ArrayList<>();

        public Attachment() {
        }

        public Attachment(final String title) {
            this.title = title;
        }

        public Attachment(final String title, final String text) {
            this.title = title;
            this.text = text;
            mrkdwn_in.add("text");
            mrkdwn_in.add("fields");
        }

        public static class Field {

            public String title;
            public String value;

            @JsonProperty(value = "short")
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
