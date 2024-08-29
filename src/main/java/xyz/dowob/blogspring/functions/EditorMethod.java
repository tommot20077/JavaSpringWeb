package xyz.dowob.blogspring.functions;

import org.springframework.web.multipart.MultipartFile;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
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
                        return false;
                    }
                } else if (insertValue instanceof Map) {
                    Map<String, Object> insertMap = (Map<String, Object>) insertValue;
                    if (insertMap.containsKey("image")) {
                        return false;
                    }

                }
            }
        }
        return true;
    }

    public static void deleteImage(String imageUrl) throws Postdata_UpdateException {
        try {
            UserConfig userConfig = UserConfig.standardSetupCommand("config.properties");
            String extraPrefix = "/extra";
            String relativePathStr = imageUrl.startsWith(extraPrefix) ? imageUrl.substring(extraPrefix.length()) : imageUrl;
            String imagePathString = userConfig.getImagePath() + "/";
            Path imagePath = Paths.get(imagePathString);

            if (Files.exists(imagePath)) {
                String deletePathString = imagePathString + relativePathStr;
                Path deletePath = Paths.get(deletePathString);
                Files.deleteIfExists(deletePath);
            }
        } catch (IOException e) {
            throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.IMAGE_DELETE_ERROR);
        }
    }
}
