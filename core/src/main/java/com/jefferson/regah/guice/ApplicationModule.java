package com.jefferson.regah.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

public class ApplicationModule extends AbstractModule {
    private static final Logger log = LogManager.getLogger(ApplicationModule.class);

    @Override
    protected void configure() {
        // possible alternative: https://dzone.com/articles/flexible-configuration-guice
        final Properties properties = new Properties();
        final File propertiesFile = new File("regah.properties");
        if (propertiesFile.exists()) {
            try (final InputStream is = new FileInputStream(propertiesFile)) {
                properties.load(is);
            } catch (FileNotFoundException e) {
                log.error("properties file doesn't exist at {}", propertiesFile);
            } catch (IOException e) {
                log.error("Error while reading properties file.");
            }
        } else {
            properties.putAll(DefaultProperties.properties);
        }

        Names.bindProperties(binder(), properties);
    }

    private static class DefaultProperties {
        private static final Map<String, String> properties = new ImmutableMap.Builder<String, String>()
                .put("sharing-server-port", "42424")
                .put("sharing-client-port", "42421")
                .put("notification-server-port", "42100")
                .put("application-data-folder", Paths.get(System.getProperty("user.home"), "regah-data").toString())
                .build();
    }
}
