package org.artyomcool.ravenproxy.impl;

import net.kencochrane.raven.DefaultRavenFactory;
import net.kencochrane.raven.Raven;
import net.kencochrane.raven.dsn.Dsn;
import org.artyomcool.ravenproxy.RavenCache;
import org.artyomcool.ravenproxy.data.FingerPrint;

public class DummyRavenCache implements RavenCache {

    private Raven raven = DefaultRavenFactory.createRavenInstance(new Dsn("http://c33b082e2b2440ce81ce1e209182734b:b3f441bd8154427e857cda9abf183cb5@sentry.orange.icq.com/2"));

    @Override
    public Raven forFingerPrint(FingerPrint fingerPrint) {
        return raven;
    }
}
