package org.artyomcool.ravenproxy;

import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class Configurator implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(Configurator.class);

    private static final String CONFIG_PATH = "configPath";

    @Autowired
    private ServletContext context;


    private String configPath;

    private Map<String, String> urls = new HashMap<>();

    private String tmpDir;

    private String mappingsDir;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String path = (String)context.getAttribute(CONFIG_PATH);
        configure(path);
    }

    public String getRavenUrl(String appName) {
        return urls.get(appName);
    }

    public String getTmpDir() {
        return tmpDir;
    }

    public String getMappingsDir() {
        return mappingsDir;
    }

    private void configure(String path) {
        if (path == null) {
            throw new IllegalStateException("There is no 'configPath' context parameter");
        }
        synchronized (this) {
            if (!path.equals(configPath)) {
                readConfig(path);
                configPath = path;
            }
        }
    }

    @GuardedBy("this")
    private void readConfig(String configPath) {
        logger.info("Loading config {}", configPath);
        urls.clear();
        try (FileInputStream inputStream = new FileInputStream(configPath)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach(this::processPropertiesEntry);

            tmpDir = properties.getProperty("config.tmpDir", "tmp");
            logger.info("Tmp {}", tmpDir);

            mappingsDir = properties.getProperty("config.mappingsDir", "mappings");
            logger.info("Mappings {}", mappingsDir);

        } catch (FileNotFoundException e) {
            logger.error("No config file " + configPath, e);
        } catch (IOException e) {
            logger.error("Can't parse config file " + configPath, e);
        }
    }

    private void processPropertiesEntry(Object key, Object value) {
        String name = key.toString();
        if (name.startsWith("apps.")) {
            if (name.endsWith(".url")) {
                name = name.substring("apps.".length(), name.length() - ".url".length());
                logger.info("Map app {}={}", name, value);
                urls.put(name, value.toString());
            }
        }
    }
}
