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

package de.gesundkrank.mrfoosball.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Properties {

    private static final Path GLOBAL_PROPERTIES_PATH =
            Paths.get("/opt/mrfoosball/conf/mrfoosball.properties");
    private static final Path PROJECT_PROPERTIES_PATH = Paths.get("./mrfoosball.properties");

    private static volatile Properties INSTANCE;

    public static Properties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Properties();
        }
        return INSTANCE;
    }

    private final java.util.Properties properties;

    private Properties() {
        final Logger logger = LogManager.getLogger();
        this.properties = new java.util.Properties();

        try {
            if (Files.exists(GLOBAL_PROPERTIES_PATH)) {
                try (final var is = Files.newInputStream(GLOBAL_PROPERTIES_PATH)) {
                    properties.load(is);
                }
            } else if (Files.exists(PROJECT_PROPERTIES_PATH)) {
                try (final var is = Files.newInputStream(PROJECT_PROPERTIES_PATH)) {
                    properties.load(is);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load configuration.", e);
        }

        this.properties.putAll(System.getenv());

        logger.info("Properties: {}", this.properties);
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("port", "8080"));
    }

    public String getConnectionUrl() {
        return properties.getProperty("connectionUrl");
    }

    public String getConnectionDriverClass() {
        return properties.getProperty("connectionDriverClass", "org.postgresql.Driver");
    }

    public String getConnectionDialect() {
        return properties
                .getProperty("connectionDialect", "org.hibernate.dialect.PostgreSQLDialect");
    }

    public String getConnectionUsername() {
        return properties.getProperty("connectionUsername", "root");
    }

    public String getConnectionPassword() {
        return properties.getProperty("connectionPassword", "");
    }

    public String getHbm2Ddl() {
        return properties.getProperty("connectionHbm2ddl", "validate");
    }

    public String zookeeperHosts() {
        return properties.getProperty("zookeeperHosts");
    }

    public String getZookeeperRootPath() {
        return properties.getProperty("zookeeperRootPath", "/mrfoosball");
    }

    public String getAppUrl() {
        return properties.getProperty("appUrl", "http://localhost:8080");
    }

    public int getQRCodeSize() {
        return Integer.parseInt(properties.getProperty("qrCodeSize", "400"));
    }

    public String getSlackClientId() {
        return properties.getProperty("slackClientId");
    }

    public String getSlackClientSecret() {
        return properties.getProperty("slackClientSecret");
    }

    public String getSlackSigningSecret() {
        return properties.getProperty("slackSigningSecret");
    }
}
