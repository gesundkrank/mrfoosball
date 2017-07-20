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
import java.util.List;

import javax.persistence.TypedQuery;


public class Store implements Closeable {

    private final static SessionFactory sessionFactory;

    static {
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
        configuration.setProperty("hibernate.hbm2ddl.auto", properties.getHbm2Ddl());

        sessionFactory = configuration.buildSessionFactory();
    }

    private final Session session;


    public Store() {
        this.session = sessionFactory.openSession();
    }

    public void newTournament(final List<Player> players, final int bestOfN) {
        Transaction tx = session.beginTransaction();

        for (Player player : players) {
            session.saveOrUpdate(player);
        }

        Tournament tournament = new Tournament();
        tournament.state = State.RUNNING;
        tournament.bestOfN = bestOfN;

        Team teamA = getTeam(players.get(0), players.get(1));
        Team teamB = getTeam(players.get(2), players.get(3));

        tournament.teamA = teamA;
        tournament.teamB = teamB;

        tournament.id = (int) session.save(tournament);
        tx.commit();
    }

    public Player getPlayer(final String id) {
        final Player player = new Player();
        player.id = id;
        return session.get(Player.class, id);
    }

    private Team getTeam(final Player player1, final Player player2) {
        final Team team = new Team();

        if (player1.compareTo(player2) > 0) {
            team.player1 = player2;
            team.player2 = player1;
        } else {
            team.player1 = player1;
            team.player2 = player2;
        }

        if (session.get(Team.class, team) == null) {
            session.save(team);
        }

        return team;
    }

    public void addMatch(final int tournamentId) throws InvalidTournamentStateException {
        final Transaction tx = session.beginTransaction();

        final Tournament tournament = session.get(Tournament.class, tournamentId);

        int teamAWins = 0;
        int teamBWins = 0;

        for (final Match match : tournament.matches) {
            if (match.state == State.FINISHED) {
                if (match.teamA > match.teamB) {
                    teamAWins++;
                } else {
                    teamBWins++;
                }
            }
        }

        final int maxTeamWins = (tournament.bestOfN / 2) + 1;

        if (maxTeamWins <= Math.max(teamAWins, teamBWins)) {
            tx.rollback();
            throw new InvalidTournamentStateException("Cannot create more matches than bestOfN.");
        }

        Match match = new Match();
        tournament.matches.add(match);

        tx.commit();
    }

    public boolean hasRunningTournament() {
        return !session.createNamedQuery("get_tournaments_with_state")
                .setParameter("state", State.RUNNING).list().isEmpty();
    }

    public List<Tournament> getTournaments() {
        TypedQuery<Tournament> query = session.createNamedQuery(
                "get_tournaments_with_state", Tournament.class)
                .setParameter("state", State.FINISHED);
        return query.getResultList();
    }

    public void updateTournament(final Tournament tournament) {
        Transaction tx = session.beginTransaction();
        session.saveOrUpdate(tournament);
        tx.commit();
    }

    public void deleteTournament(final Tournament tournament) {
        Transaction tx = session.beginTransaction();
        session.delete(tournament);
        tx.commit();
    }

    public Tournament getRunningTournament() {
        TypedQuery<Tournament> query = session.createNamedQuery(
                "get_tournaments_with_state", Tournament.class)
                .setParameter("state", State.RUNNING);
        return query.getSingleResult();
    }

    @Override
    public void close() {
        session.close();
    }

    public static class InvalidTournamentStateException extends Exception {

        public InvalidTournamentStateException(final String message) {
            super(message);
        }
    }
}
