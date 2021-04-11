package com.asgarov.finder.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

    private static final String DEFAULT_PROPERTIES_FILE = "application.properties";

    public static Properties getProperties() {
        return getProperties(DEFAULT_PROPERTIES_FILE);
    }

    public static Properties getProperties(String fileName) {
        try (InputStream input = PropertiesReader.class.getClassLoader().getResourceAsStream(fileName)) {
            Properties properties = new Properties();
            if (input == null) {
                throw new IllegalArgumentException(fileName + " not found!");
            }
            properties.load(input);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Application can't run without properties!");
        }
    }
}
