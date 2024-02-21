package xyz.dowob.blogspring.config;

import java.io.*;
import java.util.Properties;

public class UserConfig {
    private Properties properties;
    private File configFile;

    public UserConfig(String fileName) {
        this.configFile = new File(fileName);
        this.properties = new Properties();

        if (configFile.exists()) {
            System.out.println("Config file exists. Loading properties.");
            loadProperties();
        } else {
            System.out.println("Config file does not exist. Setting up default properties.");
            setupDefaultProperties();
        }
    }

    private void setupDefaultProperties() {
        properties.setProperty("rclonePath", "rclone");
        properties.setProperty("rcloneConfigPath", "/root/.config/rclone/rclone.conf");
        properties.setProperty("imagePath", "/root/JavaWeb/TempSave");
        properties.setProperty("tempSavePath", "/root/JavaWeb/tempSavePath");
        properties.setProperty("rcloneUploadPath", "webdav:emby/od/60/images");
        saveProperties();
    }


    private void loadProperties() {
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveProperties() {
        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, "Configuration for My Java Application");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }


    public String getRclonePath() {
        return properties.getProperty("rclonePath");
    }
    public String getRcloneConfigPath() {
        return properties.getProperty("rcloneConfigPath");
    }
    public String getImagePath() {
        return properties.getProperty("imagePath");
    }
    public String getTempSavePath() {
        return properties.getProperty("tempSavePath");
    }
    public String getRcloneUploadPath() {
        return properties.getProperty("rcloneUploadPath");
    }





}

