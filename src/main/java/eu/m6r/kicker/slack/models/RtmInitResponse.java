package eu.m6r.kicker.slack.models;

public class RtmInitResponse {
    public boolean ok;
    public String url;

    public String error = null;
    public String warning = null;

    public Self self;

    public static class Self {
        public String id;
    }

}


