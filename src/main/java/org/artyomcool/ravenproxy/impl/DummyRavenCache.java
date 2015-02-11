package org.artyomcool.ravenproxy.impl;

import net.kencochrane.raven.DefaultRavenFactory;
import net.kencochrane.raven.Raven;
import net.kencochrane.raven.dsn.Dsn;
import org.artyomcool.ravenproxy.RavenCache;
import org.artyomcool.ravenproxy.data.FingerPrint;

public class DummyRavenCache implements RavenCache {

    private Raven raven = DefaultRavenFactory.createRavenInstance(new Dsn("http://d60db8b4b22645ca8555232790d47146:83ca8e58f4604afcb1f15e77f3302b76@ec2-54-187-103-202.us-west-2.compute.amazonaws.com:9000/2"));

    @Override
    public Raven forFingerPrint(FingerPrint fingerPrint) {
        return raven;
    }
}
