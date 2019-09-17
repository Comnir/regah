package com.jefferson.regah.guice;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class ConfigurationModule extends AbstractModule {
    private static final Logger log = LogManager.getLogger(ConfigurationModule.class);

    private final Map<String, String> optionalDefaultProperties;

    public ConfigurationModule() {
        this(Collections.emptyMap());
    }

    public ConfigurationModule(Map<String, String> properties) {
        this.optionalDefaultProperties = Objects.requireNonNull(properties);
    }

    @Override
    protected void configure() {
        // possible alternative: https://dzone.com/articles/flexible-configuration-guice
        final Properties properties = new Properties();
        properties.putAll(DEFAULT_PROPERTIES);
        properties.putAll(optionalDefaultProperties);

        final File propertiesFile = new File("regah.properties");
        if (!propertiesFile.exists()) {
            log.info("Default configuration will be used. Configuration file doesn't exist at {}", propertiesFile.getAbsolutePath());
        } else {
            log.info("Loading configuration from file: {}", propertiesFile);
            try (final InputStream is = new FileInputStream(propertiesFile)) {
                properties.load(is);
            } catch (FileNotFoundException e) {
                log.error("properties file doesn't exist at {}", propertiesFile);
            } catch (IOException e) {
                log.error("Error while reading properties file.");
            }
        }

        Names.bindProperties(binder(), properties);
    }

    public static final String APPLICATION_DATA_FOLDER = "application-data-folder";
    private static final ImmutableMap<String, String> DEFAULT_PROPERTIES = new ImmutableMap.Builder<String, String>()
            .put("sharing-server-port", "42424")
            .put("sharing-management-port", "42421")
            .put("notification-server-port", "42100")
            .put(APPLICATION_DATA_FOLDER, Paths.get(System.getProperty("user.home"), "regah-data").toString())
            .build();
}
