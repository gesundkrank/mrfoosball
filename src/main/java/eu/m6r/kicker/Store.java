package eu.m6r.kicker;

import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.PlayerSkill;
import eu.m6r.kicker.models.State;
import eu.m6r.kicker.models.Team;
import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.trueskill.TrueSkillCalculator;
import eu.m6r.kicker.utils.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;

import java.io.Closeable;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
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

    public Player getPlayer(final Player player) {
        final Player storedPlayer = session.get(Player.class, player.id);
        if (storedPlayer != null) {
            player.trueSkillMean = storedPlayer.trueSkillMean;
            player.trueSkillStandardDeviation = storedPlayer.trueSkillStandardDeviation;
        } else {
            session.save(player);
            return getPlayer(player);
        }
        return player;
    }

    public Player getPlayer(final String id) {
        return session.get(Player.class, id);
    }

    public Team getTeam(final Player player1, final Player player2) {
        Transaction tx = session.beginTransaction();

        session.saveOrUpdate(player1);
        session.saveOrUpdate(player2);

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

        tx.commit();
        return team;
    }

    public List<Tournament> getTournaments() {
        final TypedQuery<Tournament> query = session
                .createNamedQuery("get_tournaments_with_state", Tournament.class)
                .setParameter("state", State.FINISHED);
        return query.getResultList();
    }

    public List<Tournament> getLastTournaments(int num) {
        final TypedQuery<Tournament> query = session
                .createNamedQuery("get_tournaments_with_state", Tournament.class)
                .setParameter("state", State.FINISHED)
                .setMaxResults(num);
        return query.getResultList();
    }

    public List<PlayerSkill> playerSkills() {
        final List<Object[]> list = session
                .createNativeQuery("SELECT player.id AS id, name, avatarImage, "
                                   + "COUNT(tournament.id) AS games, "
                                   + "(trueSkillMean - 3 * trueSkillStandardDeviation) AS skill "
                                   + "FROM player LEFT JOIN tournament "
                                   + "ON player.id IN (teama_player1_id, teama_player2_id, "
                                   + "teamb_player1_id, teamb_player2_id) "
                                   + "GROUP BY player.id "
                                   + "HAVING MAX(tournament.date) > (NOW() - INTERVAL  '60 days') "
                                   + "ORDER BY skill DESC")
                .addScalar("id", StandardBasicTypes.STRING)
                .addScalar("name", StandardBasicTypes.STRING)
                .addScalar("avatarImage", StandardBasicTypes.STRING)
                .addScalar("games", StandardBasicTypes.INTEGER)
                .addScalar("skill", StandardBasicTypes.DOUBLE).list();
        return list.stream().map(PlayerSkill::new).collect(Collectors.toList());
    }

    public void saveTournament(final Tournament tournament) {
        Transaction tx = session.beginTransaction();
        session.update(tournament.teamA);
        session.update(tournament.teamA.player1);
        session.update(tournament.teamA.player2);
        session.update(tournament.teamB);
        session.update(tournament.teamB.player1);
        session.update(tournament.teamB.player2);
        session.save(tournament);
        tx.commit();
    }

    public void resetPlayerSkills() {
        final Transaction tx = session.beginTransaction();
        session.createQuery("UPDATE Player SET trueSkillMean = :mean, "
                            + "trueSkillStandardDeviation = :standardDeviation")
                .setParameter("mean", TrueSkillCalculator.DEFAULT_INITIAL_MEAN)
                .setParameter("standardDeviation",
                              TrueSkillCalculator.DEFAULT_INITIAL_STANDARD_DEVIATION)
                .executeUpdate();
        tx.commit();
    }

    @Override
    public void close() {
        session.close();
    }
}
