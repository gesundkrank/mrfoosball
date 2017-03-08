package eu.m6r.kicker;

import eu.m6r.kicker.slack.Bot;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.bind.JAXBException;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    private static final String BASE_URI = "http://0.0.0.0:%d/";
    private static final Path GLOBAL_PROPERTIES_PATH =
            Paths.get("/opt/kicker/conf/kicker.properties");
    private static final Path PROJECT_PROPERTIES_PATH = Paths.get("./kicker.properties");

    public static Properties readProperties() throws IOException {
        Properties properties = new Properties();
        if (Files.exists(GLOBAL_PROPERTIES_PATH)) {
            properties.load(Files.newInputStream(GLOBAL_PROPERTIES_PATH));
        } else if (Files.exists(PROJECT_PROPERTIES_PATH)) {
            properties.load(Files.newInputStream(PROJECT_PROPERTIES_PATH));
        }

        return properties;
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(int port) {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig().packages("eu.m6r.kicker.api");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        URI uri = URI.create(String.format(BASE_URI, port));
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
        HttpHandler httpHandler = new CLStaticHttpHandler(Main.class.getClassLoader(), "www/");
        httpServer.getServerConfiguration().addHttpHandler(httpHandler, "/frontend");
        return httpServer;
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JAXBException, InterruptedException {

        final Properties properties = readProperties();

        int port = Integer.parseInt(properties.getProperty("port", "8080"));
        String slackToken = properties.getProperty("slackToken");
        try {

            startServer(port);

            new Bot(slackToken);
            //Keeps process running
            Thread.currentThread().join();
        } finally {
            Controller.INSTANCE.close();
        }
    }
}

