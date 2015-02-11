package net.kencochrane.raven;

import net.kencochrane.raven.connection.Connection;
import net.kencochrane.raven.environment.RavenEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Raven {
    private static final Logger logger = LoggerFactory.getLogger(Raven.class);
    private Connection connection;

    /**
     * Sends a raw event to the Sentry server.
     *
     * @param rawEvent event to send to Sentry.
     */
    public void sendEvent(byte[] rawEvent) {
        try {
            connection.send(rawEvent);
        } catch (Exception e) {
            logger.error("An exception occurred while sending the event to Sentry.", e);
        }
    }

    /**
     * Closes the connection for the Raven instance.
     */
    public void closeConnection() {
        try {
            connection.close();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't close the Raven connection", e);
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public String toString() {
        return "Raven{"
                + "name=" + RavenEnvironment.NAME
                + ", connection=" + connection
                + '}';
    }
}
