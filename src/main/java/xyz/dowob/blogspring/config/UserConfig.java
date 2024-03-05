package xyz.dowob.blogspring.config;

import lombok.extern.slf4j.Slf4j;
import xyz.dowob.blogspring.functions.StorageMethod;

import java.io.*;
import java.util.Properties;
@Slf4j
public class UserConfig {
    private final Properties properties;
    private final File configFile;


    public UserConfig(String fileName) {
        this.configFile = new File(fileName);
        this.properties = new Properties();

        if (configFile.exists()) {
            log.info("已存在設定檔，載入設定");
            loadProperties();
        } else {
            log.info("未存在設定檔，新增預設設定");
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
            log.error("讀取設定檔錯誤: " + ex.getMessage());
        }
    }

    private void saveProperties() {
        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, "Configuration for My Java Application");
        } catch (IOException io) {
            log.error("儲存設定檔錯誤: " + io.getMessage());
        }
    }

    public static UserConfig standardSetupCommand(String config){
        String jarPath = StorageMethod.getRunningDirectory();
        String configPath = jarPath + File.separator + config;
        return new UserConfig(configPath);
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

