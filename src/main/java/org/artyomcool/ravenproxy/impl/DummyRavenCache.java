package org.artyomcool.ravenproxy.impl;

import net.kencochrane.raven.DefaultRavenFactory;
import net.kencochrane.raven.Raven;
import net.kencochrane.raven.dsn.Dsn;
import org.artyomcool.ravenproxy.RavenCache;
import org.artyomcool.ravenproxy.data.FingerPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class DummyRavenCache implements RavenCache {

    private static final Logger logger = LoggerFactory.getLogger(DummyRavenCache.class);

    @Autowired
    private ServletContext context;

    private String configPath;

    private Map<String, Raven> ravens = new HashMap<>();

    @Override
    public Raven forFingerPrint(FingerPrint fingerPrint) {
        String path = (String)context.getAttribute("configPath");
        if (path == null) {
            throw new IllegalStateException("There is no 'configPath' context parameter");
        }
        if (!path.equals(configPath)) {
            readConfig(path);
            configPath = path;
        }
        Raven raven = ravens.get(fingerPrint.getApp());
        if (raven == null) {
            throw new IllegalArgumentException("Unknown application: " + fingerPrint);
        }
        return raven;
    }

    private void readConfig(String configPath) {
        logger.info("Loading config {}", configPath);
        ravens.clear();
        try (FileInputStream inputStream = new FileInputStream(configPath)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((name, value) -> {
                logger.info("Map app {}={}", name, value);
                ravens.put(name.toString(), DefaultRavenFactory.createRavenInstance(new Dsn(value.toString())));
            });
        } catch (FileNotFoundException e) {
            logger.error("No config file " + configPath, e);
        } catch (IOException e) {
            logger.error("Can't parse config file " + configPath, e);
        }
    }

}
