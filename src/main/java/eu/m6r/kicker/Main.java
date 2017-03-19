package eu.m6r.kicker;

import eu.m6r.kicker.slack.Bot;
import eu.m6r.kicker.utils.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

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
    public static HttpServer startServer(int port) {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig().packages("eu.m6r.kicker.api");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        URI uri = URI.create(String.format(BASE_URI, port));
        LOGGER.info("Starting web server on {}.", uri);

        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
        HttpHandler httpHandler = new CLStaticHttpHandler(Main.class.getClassLoader(), "www/");
        httpServer.getServerConfiguration().addHttpHandler(httpHandler, "/frontend");
        return httpServer;
    }

    /**
     * Main method.
     */
    public static void main(String[] args) throws InterruptedException {
        LOGGER.info("Starting kicker app.");
        try {
            try {
                final Properties properties = Properties.getInstance();

                final Bot bot = new Bot(properties.getSlackToken());
                bot.startNewSession();

                final int port = properties.getPort();

                startServer(port);

                //Keeps process running
                Thread.currentThread().join();
            } finally {
                LOGGER.info("Shutting down kicker app.");
                Controller.INSTANCE.close();
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}

