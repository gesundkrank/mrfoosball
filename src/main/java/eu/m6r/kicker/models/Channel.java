package eu.m6r.kicker.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
        @NamedQuery(
                name = "get_channel",
                query = "FROM Channel WHERE id = :id"
        ),
        @NamedQuery(
                name = "get_channel_by_slack_id",
                query = "FROM Channel WHERE slackId = :slackId"
        )
})
@Entity
@Table
public class Channel {

    @Id
    public String id;
    public String slackId;
    public String name;

    public Channel() {

    }

    public Channel(final String id, final String slackId, final String name) {
        this.id = id;
        this.slackId = slackId;
        this.name = name;
    }
}
