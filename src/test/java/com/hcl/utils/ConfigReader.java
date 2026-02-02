package com.hcl.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfigReader.java
 * Loads configuration properties from a file.
 */
public class ConfigReader {

    private static Properties prop;

    /**
     * Load properties from given file path
     * @return 
     */
    public static ConfigReader loadProperties(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            prop = new Properties();
            prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load config.properties file: " + e.getMessage());
        }
		return null;
    }

    /**
     * Get property value by key
     */
    public String getProperty(String key) {
        if (prop == null) {
            throw new RuntimeException("Properties not loaded. Call loadProperties() first.");
        }
        return prop.getProperty(key);
    }

    /**
     * Get property value by key with default
     */
    public static String getProperty(String key, String defaultValue) {
        if (prop == null) {
            throw new RuntimeException("Properties not loaded. Call loadProperties() first.");
        }
        return prop.getProperty(key, defaultValue);
    }
}
