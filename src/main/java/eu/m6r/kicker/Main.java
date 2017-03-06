package eu.m6r.kicker;

import org.glassfish.grizzly.http.server.HttpHandler;
import eu.m6r.kicker.slack.Bot;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

import javax.xml.bind.JAXBException;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:%d/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(int port, String assetDir) {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig().packages("eu.m6r.kicker.api");


        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        URI uri = URI.create(String.format(BASE_URI, port));
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
        HttpHandler httpHandler = new StaticHttpHandler(assetDir);

        httpServer.getServerConfiguration().addHttpHandler(httpHandler, "/static");
        return httpServer;
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, JAXBException {
        // TODO remove after slack connection works
        try {
//            Controller.INSTANCE.createTestTournament();
            int port = 8080;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }

            String assetDir = "/";
            if (args.length >= 2) {
                assetDir = args[1];
            }

            final HttpServer server = startServer(port, assetDir);
            new Bot("xoxb-147993519888-On7vrmgxLoviTGYXrSl0hLnZ");
            System.out.println(String.format("Jersey app started with WADL available at "
                                             + "%sapplication.wadl\nHit enter to stop it...",
                                             BASE_URI));
            System.in.read();
            server.stop();
        } finally {
            Controller.INSTANCE.close();
        }
    }
}

