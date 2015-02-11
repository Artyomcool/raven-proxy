package net.kencochrane.raven.connection;

import java.io.Closeable;

/**
 * Connection to a Sentry server, allowing to send captured events.
 */
public interface Connection extends Closeable {

    /**
     * Sends a raw event to the sentry server.
     *
     * @param raw an event in a raw form to add in Sentry.
     */
    void send(byte[] raw);
}
