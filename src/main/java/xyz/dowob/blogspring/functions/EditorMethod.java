package xyz.dowob.blogspring.functions;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditorMethod {
    public static String saveImage(MultipartFile file, Long articleId) throws IOException {
        String currentDir= StorageMethod.getRunningDirectory();
        String uploadDirPath = currentDir + "/ImageData/" + articleId;
        Path filePath = Paths.get(uploadDirPath);

        if (!Files.exists(filePath)) {
            Files.createDirectories(filePath);
        }


        String filename = UUID.randomUUID()+ "__" +file.getOriginalFilename();
        Path savePath = Paths.get(uploadDirPath).resolve(filename);
        Files.copy(file.getInputStream(), savePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

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
