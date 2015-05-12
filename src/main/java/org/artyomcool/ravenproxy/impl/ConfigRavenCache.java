package org.artyomcool.ravenproxy.impl;

import net.kencochrane.raven.DefaultRavenFactory;
import net.kencochrane.raven.Raven;
import net.kencochrane.raven.dsn.Dsn;
import org.artyomcool.ravenproxy.Configurator;
import org.artyomcool.ravenproxy.RavenCache;
import org.artyomcool.ravenproxy.data.FingerPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConfigRavenCache implements RavenCache {

    private static final Logger logger = LoggerFactory.getLogger(ConfigRavenCache.class);

    @Autowired
    private Configurator configurator;

    private Map<String, Raven> ravens = new ConcurrentHashMap<>();

    @Override
    public Raven forFingerPrint(FingerPrint fingerPrint) {
        String appName = fingerPrint.getApp();
        return ravens.computeIfAbsent(appName, this::createRavenFromConfig);
    }

    private Raven createRavenFromConfig(String appName) {
        String url = configurator.getRavenUrl(appName);
        if (url == null) {
            throw new IllegalArgumentException("Unknown application: " + appName);
        }
        return DefaultRavenFactory.createRavenInstance(new Dsn(url));
    }

}
