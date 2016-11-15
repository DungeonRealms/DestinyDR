package net.dungeonrealms.control.config;

import net.dungeonrealms.control.Control;
import net.dungeonrealms.control.utils.UtilLogger;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class Configuration {

    private File file;
    private Properties properties;

    public Configuration(File file) {
        this.file = file;
    }

    public void load() throws IOException {
        // If file does not exist we create one.
        if (!file.exists()) {
            Files.copy(Control.class.getResourceAsStream("/config.properties"), file.toPath());
        }

        // Convert the file to an input stream.
        InputStream inputStream = new FileInputStream(file);

        // Load the properties from the input stream.
        properties = new Properties();
        properties.load(inputStream);
    }

    public void save() throws IOException {
        // Convert the file to an output stream.
        FileOutputStream outputStream = new FileOutputStream(file);

        // Store the properties in the file.
        properties.store(outputStream, null);
    }

    public boolean getBoolean(String key) {
        return Boolean.valueOf(getSetting(key));
    }

    public String getSetting(String key) {
        return properties.getProperty(key);
    }

    public void setSetting(String key, String value) {
        properties.setProperty(key, value);

        try {
            save();
        } catch (Exception e) {
            UtilLogger.warn("Failed to save new config");
        }
    }
}
