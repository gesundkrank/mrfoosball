package eu.m6r.kicker.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Properties {

    private static final Path GLOBAL_PROPERTIES_PATH =
            Paths.get("/opt/kicker/conf/kicker.properties");
    private static final Path PROJECT_PROPERTIES_PATH = Paths.get("./kicker.properties");

    private static Properties INSTANCE;

    public static Properties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Properties();
        }
        return INSTANCE;
    }

    private final Logger logger;
    private final java.util.Properties properties;

    private Properties() {
        this.logger = LogManager.getLogger();
        this.properties = new java.util.Properties();

        try {
            if (Files.exists(GLOBAL_PROPERTIES_PATH)) {
                properties.load(Files.newInputStream(GLOBAL_PROPERTIES_PATH));
            } else if (Files.exists(PROJECT_PROPERTIES_PATH)) {
                properties.load(Files.newInputStream(PROJECT_PROPERTIES_PATH));
            }
        } catch (IOException e) {
            logger.error("Failed to load configuration.", e);
            throw new IOError(e);
        }
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("port", "8080"));
    }

    public String getSlackToken() {
        return properties.getProperty("slackToken");
    }

    public String getConnectionUrl() {
        return properties.getProperty("connectionUrl",
                                      "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;MVCC=TRUE");
    }

    public String getConnectionDriverClass() {
        return properties.getProperty("connectionDriverClass", "org.h2.Driver");
    }

    public String getConnectionDialect() {
        return properties.getProperty("connectionDialect", "org.hibernate.dialect.H2Dialect");
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
}
