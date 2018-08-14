package eu.m6r.kicker;

import eu.m6r.kicker.models.Channel;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.PlayerSkill;
import eu.m6r.kicker.models.State;
import eu.m6r.kicker.models.Team;
import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.trueskill.TrueSkillCalculator;
import eu.m6r.kicker.utils.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
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

    private final Logger logger;
    private final Session session;


    public Store() throws HibernateException {
        this.logger = LogManager.getLogger();

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
        final List<Object[]> list = session
                .createNativeQuery("SELECT player.id AS id, name, avatarImage, \n"
                                   + "SUM(games.games) AS games, \n"
                                   + "SUM(wins.wins) AS wins,\n"
                                   + "(trueSkillMean - 3 * trueSkillStandardDeviation) AS skill \n"
                                   + "FROM player \n"
                                   + "INNER JOIN (\n"
                                   + "  SELECT player.id AS player_id FROM player\n"
                                   + "  LEFT JOIN tournament \n"
                                   + "  ON player.id IN (teama_player1_id, teama_player2_id, teamb_player1_id, teamb_player2_id)\n"
                                   + "    AND tournament.channel_id = :channelId\n"
                                   + "  GROUP BY player.id\n"
                                   + "  HAVING MAX(tournament.date) > (NOW() - INTERVAL  '60 days') \n"
                                   + ") AS has_recent_tournaments ON player.id = has_recent_tournaments.player_id\n"
                                   + "LEFT JOIN (\n"
                                   + "  SELECT player.id AS player_id, COUNT(tournament.id) AS games FROM player\n"
                                   + "  LEFT JOIN tournament \n"
                                   + "  ON player.id IN (teama_player1_id, teama_player2_id, teamb_player1_id, teamb_player2_id)\n"
                                   + "     AND tournament.channel_id = :channelId\n"
                                   + "  GROUP BY player_id\n"
                                   + ") AS games ON player.id = games.player_id \n"
                                   + "LEFT JOIN (\n"
                                   + "  SELECT player.id AS player_id, COUNT(*) AS wins FROM player LEFT JOIN (\n"
                                   + "    SELECT \n"
                                   + "      (CASE WHEN COALESCE(wins_a, 0) > COALESCE(wins_b, 0) THEN teama_player1_id ELSE teamb_player1_id END) AS winner_1,\n"
                                   + "      (CASE WHEN COALESCE(wins_a, 0) > COALESCE(wins_b, 0) THEN teama_player2_id ELSE teamb_player2_id END) AS winner_2\n"
                                   + "      FROM tournament LEFT JOIN (\n"
                                   + "      SELECT tournament_id, COUNT(matches_id) AS wins_a \n"
                                   + "      FROM tournament_match \n"
                                   + "      INNER JOIN match ON matches_id = match.id\n"
                                   + "      WHERE teama > teamb\n"
                                   + "      GROUP BY tournament_id\n"
                                   + "    ) AS team_a ON tournament.id = team_a.tournament_id\n"
                                   + "    LEFT JOIN (\n"
                                   + "      SELECT tournament_id, COUNT(matches_id) AS wins_b \n"
                                   + "      FROM tournament_match \n"
                                   + "      INNER JOIN match ON matches_id = match.id\n"
                                   + "      WHERE teama < teamb\n"
                                   + "      GROUP BY tournament_id\n"
                                   + "    ) AS team_b ON tournament.id = team_b.tournament_id\n"
                                   + "    WHERE channel_id = :channelId\n"
                                   + "  ) AS winners ON player.id IN (winner_1, winner_2)\n"
                                   + "  GROUP BY player_id\n"
                                   + ") AS wins ON player.id = wins.player_id\n"
                                   + "GROUP BY player.id \n"
                                   + "ORDER BY skill DESC;")
                .setParameter("channelId", channelId)
                .addScalar("id", StandardBasicTypes.STRING)
                .addScalar("name", StandardBasicTypes.STRING)
                .addScalar("avatarImage", StandardBasicTypes.STRING)
                .addScalar("games", StandardBasicTypes.INTEGER)
                .addScalar("wins", StandardBasicTypes.INTEGER)
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

    @Override
    public void close() {
        session.close();
    }
}
