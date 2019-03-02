package eu.m6r.kicker;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.StandardBasicTypes;

import eu.m6r.kicker.models.Channel;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.PlayerSkill;
import eu.m6r.kicker.models.State;
import eu.m6r.kicker.models.Team;
import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.utils.Properties;


public class Store implements Closeable {

    private static final SessionFactory sessionFactory;

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

    private final Logger logger;
    private final Session session;


    public Store() throws HibernateException {
        logger = LogManager.getLogger();

        try {
            this.session = sessionFactory.openSession();
        } catch (Exception e) {
            logger.error(e);
            throw e;
        }
    }

    public Channel getChannel(final String id) {
        final TypedQuery<Channel> query = session
                .createNamedQuery("get_channel", Channel.class)
                .setParameter("id", id);
        return query.getSingleResult();
    }

    public boolean channelExists(final String id) {
        final TypedQuery<Channel> query = session
                .createNamedQuery("get_channel", Channel.class)
                .setParameter("id", id);
        return !query.getResultList().isEmpty();
    }

    public Channel getChannelBySlackId(final String slackId) {
        final TypedQuery<Channel> query = session
                .createNamedQuery("get_channel_by_slack_id", Channel.class)
                .setParameter("slackId", slackId);
        return query.getSingleResult();
    }

    public void saveChannel(final Channel channel) {
        final Transaction tx = session.beginTransaction();
        session.saveOrUpdate(channel);
        tx.commit();
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

    public List<Tournament> getTournaments(final String channelId) {
        final Channel channel = new Channel();
        channel.id = channelId;
        final TypedQuery<Tournament> query = session
                .createNamedQuery("get_tournaments_with_state", Tournament.class)
                .setParameter("channel", channel)
                .setParameter("state", State.FINISHED);
        return query.getResultList();
    }

    public List<Tournament> getLastTournaments(final String channelId, int num) {
        final Channel channel = new Channel();
        channel.id = channelId;
        final TypedQuery<Tournament> query = session
                .createNamedQuery("get_tournaments_with_state", Tournament.class)
                .setParameter("channel", channel)
                .setParameter("state", State.FINISHED)
                .setMaxResults(num);
        return query.getResultList();
    }

    public List<PlayerSkill> playerSkills(final String channelId) {
        final var queryFile = getClass().getResource("player_skill.sql").getFile();
        try {
            final var query = new String(Files.readAllBytes(Paths.get(queryFile)));
            final List<Object[]> list = session
                    .createNativeQuery(query)
                    .setParameter("channelId", channelId)
                    .addScalar("id", StandardBasicTypes.STRING)
                    .addScalar("name", StandardBasicTypes.STRING)
                    .addScalar("avatarImage", StandardBasicTypes.STRING)
                    .addScalar("games", StandardBasicTypes.INTEGER)
                    .addScalar("wins", StandardBasicTypes.INTEGER)
                    .addScalar("skill", StandardBasicTypes.DOUBLE).list();
            return list.stream().map(PlayerSkill::new).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Failed to load query", e);
            return Collections.emptyList();
        }

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

    @Override
    public void close() {
        session.close();
    }
}
