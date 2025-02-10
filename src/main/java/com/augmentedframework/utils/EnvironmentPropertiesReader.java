package com.augmentedframework.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

/**
 * EnvironmentPropertiesReader sets the environment variable from config properties file
 *
 * @author YSabato
 * @version 1.0
 * @since 01/01/2020
 */
public class EnvironmentPropertiesReader {
    private static final Logger log = LoggerFactory.getLogger(EnvironmentPropertiesReader.class);
    private static EnvironmentPropertiesReader envProps;

    private Properties properties;

    private EnvironmentPropertiesReader() {
        properties = loadProperties();
    }

    private EnvironmentPropertiesReader(String fileName) {
        properties = loadProperties(fileName);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public boolean hasProperty(String key) {
        return StringUtils.isNotBlank(properties.getProperty(key));
    }

    public static EnvironmentPropertiesReader getInstance() {
        if (envProps == null) {
            envProps = new EnvironmentPropertiesReader();
        }
        return envProps;
    }

    /**
     * Method reads and loads config.properties file from
     * source root in main resource folder
     *
     * @return map of property key, value pairs
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = EnvironmentPropertiesReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                log.error("config.properties is missing or corrupt");
                return props;
            }
            props.load(input);
        } catch (IOException e) {
            log.error("Read failed due to: " + e.getMessage());
        }
        return props;
    }

    /**
     * Method reads content from source fileName
     *
     * @param fileName - source file path
     * @return map of property key, value pairs
     */
    private Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream input = EnvironmentPropertiesReader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                log.error(fileName + " is missing or corrupt");
                return props;
            }
            props.load(input);
        } catch (IOException e) {
            log.error("Read failed due to: " + e.getMessage());
        }
        return props;
    }
}
