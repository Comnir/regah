package com.jefferson.regah.guice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationModuleTest {
    @Test
    void propertiesFromMapOverriden_when_loadingFromFile(@TempDir File tempDir) throws IOException {
        final File propertiesFile = new File(tempDir, "file.properties");
        final Properties properties = new Properties();
        properties.put("a", "b");

        properties.store(new FileOutputStream(propertiesFile), "");

        final Properties otherProperties = new Properties();
        otherProperties.put("a", "c");
        otherProperties.put("another", "aValue");

        otherProperties.load(new FileReader(propertiesFile));

        assertEquals(otherProperties.getProperty("a"), "b");
        assertEquals(otherProperties.getProperty("another"), "aValue");
    }
}