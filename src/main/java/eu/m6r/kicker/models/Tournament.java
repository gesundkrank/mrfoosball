package eu.m6r.kicker.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@NamedQueries({
        @NamedQuery(
                name = "get_tournament",
                query = "FROM Tournament WHERE id = :id"
        ),
        @NamedQuery(
                name = "get_tournaments",
                query = "FROM Tournament WHERE channel = :channel"
        ),
        @NamedQuery(
                name = "get_tournaments_with_state",
                query = "FROM Tournament WHERE channel = :channel "
                        + "AND state = :state ORDER BY id DESC"
        )
})
@Entity
@Table
public class Tournament {
    public Tournament() {}

    public Tournament(final int bestOfN, final Team teamA, final Team teamB,
                      final Channel channel) {
        this.bestOfN = bestOfN;
        this.teamA = teamA;
        this.teamB = teamB;
        this.channel = channel;
    }

    @Id
    @GeneratedValue
    public int id;

    public int bestOfN = 1;

    public Date date = new Date();

    @ManyToOne
    public Team teamA;

    @ManyToOne
    public Team teamB;

    @Enumerated(EnumType.STRING)
    public State state = State.RUNNING;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Match> matches = new ArrayList<>();

    public Double teamAPlayer1SkillChange;
    public Double teamAPlayer2SkillChange;
    public Double teamBPlayer1SkillChange;
    public Double teamBPlayer2SkillChange;

    @ManyToOne
    public Channel channel;

    @Override
    public String toString() {
        return String.format("id=%d, teamA=%s, teamB=%s, state=%s, matches=%s", id, teamA,
                             teamB,
                             state, matches);
    }

    public Team winner() {
        if (state == State.RUNNING) {
            return null;
        }

        int sumMatches = 0;
        for (final Match match : matches) {
            sumMatches += match.teamA > match.teamB ? 1 : -1;
        }

        return sumMatches > 0 ? teamA : teamB;
    }
}
