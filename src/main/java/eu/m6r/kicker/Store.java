package eu.m6r.kicker;

import eu.m6r.kicker.models.Match;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.State;
import eu.m6r.kicker.models.Team;
import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.utils.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.persistence.TypedQuery;


public class Store implements Closeable {

    private final SessionFactory sessionFactory;


    public Store() {
        final Configuration configuration = new Configuration();

        final Properties properties = Properties.getInstance();
        configuration.configure();

        configuration.setProperty("hibernate.connection.url", properties.getConnectionUrl());
        configuration.setProperty("hibernate.connection.driver_class",
                                  properties.getConnectionDriverClass());
        configuration.setProperty("hibernate.dialect", properties.getConnectionDialect());
        configuration.setProperty("hibernate.connection.username",
                                  properties.getConnectionUsername());
        configuration.setProperty("hibernate.connection.password",
                                  properties.getConnectionPassword());

        this.sessionFactory = configuration.buildSessionFactory();
    }

    private Session newSession() {
        return sessionFactory.openSession();
    }

    public void newTournament(final List<Player> players) {
        try (final Session session = newSession()) {

            Transaction tx = session.beginTransaction();

            for (Player player : players) {
                session.saveOrUpdate(player);
            }

            Collections.shuffle(players);
            Tournament tournament = new Tournament();
            tournament.state = State.RUNNING;

            Team teamA = new Team();
            List<Player> team1 = players.subList(0, 2);
            team1.sort(Player::compareTo);
            teamA.player1 = team1.get(0);
            teamA.player2 = team1.get(1);

            if (!session.contains(teamA)) {
                session.save(teamA);
            }
            tournament.teamA = teamA;

            Team teamB = new Team();
            List<Player> team2 = players.subList(2, 4);
            team2.sort(Player::compareTo);
            teamB.player1 = team2.get(0);
            teamB.player2 = team2.get(1);

            if (!session.contains(teamB)) {
                session.save(teamB);
            }
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

    public void deleteTournament(final Tournament tournament) {
        try (final Session session = newSession()) {
            Transaction tx = session.beginTransaction();
            session.delete(tournament);
            tx.commit();
        }
    }

    public Tournament getRunningTournament() {
        try (final Session session = newSession()) {
            TypedQuery<Tournament> query = session.createNamedQuery(
                    "get_tournaments_with_state", Tournament.class)
                    .setParameter("state", State.RUNNING);
            return query.getSingleResult();
        }
    }

    @Override
    public void close() throws IOException {
        sessionFactory.close();
    }
}
