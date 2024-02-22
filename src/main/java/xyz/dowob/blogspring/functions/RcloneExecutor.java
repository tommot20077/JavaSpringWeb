package xyz.dowob.blogspring.functions;

import xyz.dowob.blogspring.config.UserConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RcloneExecutor {
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
            commands = new String[]{rclonePath, "move", TempSave, "webdav:emby/od/60/images", "--config", rcloneConfigPath};
        }
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        try {
            Process process = processBuilder.start();

            // 讀取命令的正常輸出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // 讀取錯誤輸出（如果存在）
            try (BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = readerError.readLine()) != null) {
                    System.err.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Rclone 執行錯誤: " + exitCode);
            }
        } catch (IOException e) {
            System.err.println("rclone文件操作錯誤: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("rclone被中斷: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
