package xyz.dowob.blogspring.functions;

import org.springframework.web.multipart.MultipartFile;
import xyz.dowob.blogspring.config.UserConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EditorMethod {

    public static Map<String, String> saveImage(MultipartFile file, Long articleId) throws IOException {
        UserConfig userConfig = UserConfig.standardSetupCommand("config.properties");

        String tempUploadDirPath = userConfig.getTempSavePath() + "/" + articleId;
        Path tempfilePath = Paths.get(tempUploadDirPath);

        if (!Files.exists(tempfilePath)) {
            Files.createDirectories(tempfilePath);
        }
        String uuid = UUID.randomUUID()+ "__";
        String filename = uuid + file.getOriginalFilename();
        Path savePath = Paths.get(tempUploadDirPath).resolve(filename);
        Files.copy(file.getInputStream(), savePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        RcloneExecutor.executeRclone(userConfig);
        return Map.of("url", "/extra/"+ articleId +"/" + filename, "originalName", Objects.requireNonNull(file.getOriginalFilename()));
    }

    public static String saveCommentImage(MultipartFile file, Long articleId) throws IOException {
        UserConfig userConfig = UserConfig.standardSetupCommand("config.properties");

        String tempUploadDirPath = userConfig.getTempSavePath() + "/" + articleId;
        Path tempfilePath = Paths.get(tempUploadDirPath);

        if (!Files.exists(tempfilePath)) {
            Files.createDirectories(tempfilePath);
        }

        String filename = UUID.randomUUID()+ "__" +file.getOriginalFilename();
        Path savePath = Paths.get(tempUploadDirPath).resolve(filename);
        Files.copy(file.getInputStream(), savePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        RcloneExecutor.executeRclone(userConfig);

        return "/extra/"+ articleId +"/" + filename;
    }

    public static boolean isOnlyWhiteSpaceOrEmpty(Map<String, Object> delta) {
        if (delta.containsKey("ops")) {
            List<Map<String, Object>> ops = (List<Map<String, Object>>) delta.get("ops");
            for (Map<String, Object> op : ops) {
                Object insertValue = op.get("insert");
                if (insertValue instanceof String){
                    String text = (String) insertValue;
                    if (text != null && !text.trim().isEmpty() && !text.equals("\n")) {
                        return false; // 有实际内容
                    }
                } else if (insertValue instanceof Map) {
                    Map<String, Object> insertMap = (Map<String, Object>) insertValue;
                    if (insertMap.containsKey("image")) {
                        return false; // 有实际内容
                    }

                }
            }
        }
        return true; // 无实际内容，只有空白或为空
    }
}
