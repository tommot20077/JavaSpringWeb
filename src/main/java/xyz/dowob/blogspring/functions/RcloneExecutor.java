package xyz.dowob.blogspring.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.dowob.blogspring.config.UserConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RcloneExecutor {
    static Logger logger = LoggerFactory.getLogger(RcloneExecutor.class);


    public static void executeRclone(UserConfig userConfig) {
        String rclonePath = userConfig.getRclonePath();
        String rcloneConfigPath = userConfig.getRcloneConfigPath();
        String TempSave = userConfig.getTempSavePath();
        String rcloneUploadPath = userConfig.getRcloneUploadPath();

        String os = System.getProperty("os.name").toLowerCase();
        String[] commands;
        if (os.contains("dow")) {
            commands = new String[]{"cmd", "/c", rclonePath, "move", TempSave, rcloneUploadPath, "--config", rcloneConfigPath, "--delete-empty-src-dirs"};
        } else {
            commands = new String[]{rclonePath, "move", TempSave, rcloneUploadPath, "--config", rcloneConfigPath, "--delete-empty-src-dirs"};
        }
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        try {
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info(line);
                }
            }

            try (BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = readerError.readLine()) != null) {
                    logger.error(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Rclone 執行錯誤: " + exitCode);
            }
        } catch (IOException e) {
            logger.error("rclone文件操作錯誤: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("rclone被中斷: " + e.getMessage());
        }
    }
}
