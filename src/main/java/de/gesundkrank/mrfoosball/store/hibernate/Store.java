/*
 * This file is part of MrFoosball (https://github.com/gesundkrank/mrfoosball).
 * Copyright (c) 2020 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.gesundkrank.mrfoosball.store.hibernate;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import de.gesundkrank.mrfoosball.models.Channel;
import de.gesundkrank.mrfoosball.models.Player;
import de.gesundkrank.mrfoosball.models.PlayerSkill;
import de.gesundkrank.mrfoosball.models.SlackWorkspace;
import de.gesundkrank.mrfoosball.models.State;
import de.gesundkrank.mrfoosball.models.Team;
import de.gesundkrank.mrfoosball.models.Team.Key;
import de.gesundkrank.mrfoosball.models.Tournament;
import de.gesundkrank.mrfoosball.utils.Properties;


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

    public boolean checkDatabase() {
        return session.isConnected() && session.isOpen();
    }
    
    public SlackWorkspace getSlackWorkSpace(final String teamId) throws NoResultException {
        final var query = session
                .createNamedQuery("get_slack_workspace", SlackWorkspace.class)
                .setParameter("teamId", teamId);
        return query.getSingleResult();
    }

    public void saveSlackWorkSpace(final SlackWorkspace workspace) {
        final Transaction tx = session.beginTransaction();
        session.saveOrUpdate(workspace);
        tx.commit();
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

    public Channel getChannelBySlackId(final String slackId) throws NoResultException {
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
        return getPlayer(player.id);
    }

    public Player getPlayer(final String id) {
        return session.get(Player.class, id);
    }

    public void savePlayer(final Player player) {
        final Transaction tx = session.beginTransaction();
        session.saveOrUpdate(player);
        tx.commit();
    }

    public Team getTeam(final Player player1, final Player player2) {
        Transaction tx = session.beginTransaction();

        session.saveOrUpdate(player1);
        session.saveOrUpdate(player2);

        final Key key = new Key();

        if (player1.compareTo(player2) > 0) {
            key.player1 = player2;
            key.player2 = player1;
        } else {
            key.player1 = player1;
            key.player2 = player2;
        }

        Team team = session.get(Team.class, key);
        if (team == null) {
            team = new Team();
            team.player1 = key.player1;
            team.player2 = key.player2;
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
        final var queryFile = getClass().getResourceAsStream("player_skill.sql");
        try {
            final var query = IOUtils.toString(queryFile, StandardCharsets.UTF_8);
            return session
                    .createNativeQuery(query, PlayerSkill.class)
                    .setParameter("channelId", channelId)
                    .list();
        } catch (IOException e) {
            logger.error("Failed to load query", e);
            return Collections.emptyList();
        }
    }

    public void saveTournament(final Tournament tournament) {
        final var tx = session.beginTransaction();
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
