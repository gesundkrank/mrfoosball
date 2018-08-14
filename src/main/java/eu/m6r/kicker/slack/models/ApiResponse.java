package eu.m6r.kicker.slack.models;

public class ApiResponse {
    public boolean ok;
    public String error;

    @Override
    public String toString() {
        return "ApiResponse{" +
               "ok=" + ok +
               ", error='" + error + '\'' +
               '}';
    }
}
