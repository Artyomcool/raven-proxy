package net.kencochrane.raven.connection;

import java.io.OutputStream;

public interface Sender {
    void send(OutputStream stream);
}
