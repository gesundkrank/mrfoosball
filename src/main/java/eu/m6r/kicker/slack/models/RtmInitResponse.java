package eu.m6r.kicker.slack.models;

public class RtmInitResponse {
    public boolean ok;
    public String url;

    public String error;
    public String warning;

    public Self self;

    public static class Self {
        public String id;
    }

}


