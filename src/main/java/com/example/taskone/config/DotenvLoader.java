package com.example.taskone.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class DotenvLoader {

    private static final Logger LOGGER = Logger.getLogger(DotenvLoader.class.getName());

    public static void load() {
        // Look for .env file in the current working directory or taskone subdirectory
        File envFile = new File(".env");
        if (!envFile.exists()) {
            envFile = new File("taskone/.env");
        }
        if (!envFile.exists()) {
            LOGGER.info(".env file not found in current directory or taskone subdirectory. Falling back to system environment variables.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalSignIdx = line.indexOf('=');
                if (equalSignIdx > 0) {
                    String key = line.substring(0, equalSignIdx).trim();
                    String value = line.substring(equalSignIdx + 1).trim();

                    // Strip optional wrapping quotes (double or single)
                    if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
                        value = value.substring(1, value.length() - 1);
                    } else if (value.startsWith("'") && value.endsWith("'") && value.length() > 1) {
                        value = value.substring(1, value.length() - 1);
                    }

                    // Set system property if not already set by system environment
                    if (System.getenv(key) != null) {
                        // System env takes precedence, but log it
                        LOGGER.fine("Environment variable " + key + " is already set in system environment.");
                    } else {
                        System.setProperty(key, value);
                    }
                }
            }
            LOGGER.info("Loaded environment variables from .env file successfully.");
        } catch (IOException e) {
            LOGGER.severe("Failed to load .env file: " + e.getMessage());
        }
    }
}
