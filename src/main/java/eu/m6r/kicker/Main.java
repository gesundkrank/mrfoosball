/*
 * This file is part of kicker (https://github.com/mbrtargeting/kicker).
 * Copyright (c) 2019 Jan Graßegger.
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

package eu.m6r.kicker;

import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import eu.m6r.kicker.models.Channel;
import eu.m6r.kicker.slack.Bot;
import eu.m6r.kicker.store.Store;
import eu.m6r.kicker.utils.Properties;
import eu.m6r.kicker.utils.ZookeeperClient;

/**
 * Main class.
 */
public class Main {

    // Base URI the Grizzly HTTP server will listen on
    private static final String BASE_URI = "http://0.0.0.0:%d/";


    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.
     */
    private static HttpServer startServer(int port) {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig().packages("eu.m6r.kicker.api");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        URI uri = URI.create(String.format(BASE_URI, port));
        LOGGER.info("Starting web server on {}.", uri);

        final HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
        final HttpHandler httpHandler =
                new CLStaticHttpHandler(Main.class.getClassLoader(), "www/");
        httpServer.getServerConfiguration().addHttpHandler(httpHandler, "/frontend");
        return httpServer;
    }

    private static void createTestChannel(final Properties properties) {
        try (final Store store = new Store()) {
            final Channel channel = new Channel(properties.getTestChannelId(),
                                                properties.getTestChannelSlackId(),
                                                properties.getTestChannelName());

            store.saveChannel(channel);
        }
    }

    /**
     * Main method.
     */
    public static void main(String[] args)
            throws Bot.StartSocketSessionException, InterruptedException, IOException,
                   KeeperException {
        LOGGER.info("Starting kicker app.");
        try {
            final Properties properties = Properties.getInstance();

            if (properties.hasTestChannel()) {
                createTestChannel(properties);
            }

            final ZookeeperClient zookeeperClient =
                    new ZookeeperClient(properties.zookeeperHosts());

            new Bot(properties.getSlackToken(), zookeeperClient);

            final int port = properties.getPort();

            startServer(port);

            //Keeps process running
            Thread.currentThread().join();
        } finally {
            LOGGER.info("Shutting down kicker app.");
        }
    }
}

