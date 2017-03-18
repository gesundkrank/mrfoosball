package eu.m6r.kicker;

import eu.m6r.kicker.models.Match;
import eu.m6r.kicker.models.State;
import eu.m6r.kicker.models.Team;
import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.models.User;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.persistence.TypedQuery;


public class Store implements Closeable {

    private static final Path PRODUCTION_CONF_PATH =
            Paths.get("/opt/kicker/conf/hibernate.cfg.xml");

    private final SessionFactory sessionFactory;


    public Store() {
        final Configuration configuration = new Configuration();

        if (Files.exists(PRODUCTION_CONF_PATH)) {
            configuration.configure(PRODUCTION_CONF_PATH.toFile());
        } else {
            configuration.configure();
        }

        this.sessionFactory = configuration.buildSessionFactory();
    }

    private Session newSession() {
        return sessionFactory.openSession();
    }

    public void newTournament(final List<User> players) {
        try (final Session session = newSession()) {

            Transaction tx = session.beginTransaction();

            for (User player : players) {
                session.saveOrUpdate(player);
            }


            Collections.shuffle(players);
            Tournament tournament = new Tournament();
            tournament.state = State.RUNNING;

            Team teamA = new Team();
            List<User> team1 = players.subList(0, 2);
            team1.sort(User::compareTo);
            teamA.player1 = team1.get(0);
            teamA.player2 = team1.get(1);

            session.save(teamA);
            tournament.teamA = teamA;

            Team teamB = new Team();
            List<User> team2 = players.subList(2, 4);
            team2.sort(User::compareTo);
            teamB.player1 = team2.get(0);
            teamB.player2 = team2.get(1);

            session.save(teamB);
            tournament.teamB = teamB;

            tournament.id = (int) session.save(tournament);
            addMatch(tournament);
            tx.commit();
        }
    }

    private void addMatch(final Tournament tournament) {
        Match match = new Match();
        tournament.matches.add(match);
    }

    public void addMatch(final int tournamentId) {
        try (final Session session = newSession()) {
            final Transaction tx = session.beginTransaction();

            final Tournament tournament = session.get(Tournament.class, tournamentId);

            addMatch(tournament);

            tx.commit();
        }
    }

    public boolean hasRunningTournament() {
        try (final Session session = newSession()) {
            return !session.createNamedQuery("get_tournaments_with_state")
                    .setParameter("state", State.RUNNING).list().isEmpty();
        }
    }

    public List<Tournament> getTournaments() {
        try (final Session session = newSession()) {
            TypedQuery<Tournament> query = session.createNamedQuery(
                    "get_tournaments", Tournament.class);
            return query.getResultList();
        }
    }

    public void updateTournament(final Tournament tournament) {
        try (final Session session = newSession()) {
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(tournament);
            tx.commit();
        }
    }

    public Tournament getRunningTournament() {
        try (final Session session = newSession()) {
            TypedQuery<Tournament> query = session.createNamedQuery(
                    "get_tournaments_with_state", Tournament.class).setParameter("state", State.RUNNING);
            return query.getSingleResult();
        }
    }

    @Override
    public void close() throws IOException {
        sessionFactory.close();
    }
}
