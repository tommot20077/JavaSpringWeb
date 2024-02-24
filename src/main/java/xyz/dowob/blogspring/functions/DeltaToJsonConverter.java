package xyz.dowob.blogspring.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class DeltaToJsonConverter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    public String convertCommentDeltaToJson(Map<String,Object> delta) throws JsonProcessingException {
        if (delta != null) {
            return objectMapper.writeValueAsString(delta);
        }
        return null;
    }

    public Map<String, Object> convertJsonToDelta(String json) throws JsonProcessingException {
        if (json != null) {
            return objectMapper.readValue(json, new TypeReference<>() {});
        }
        return null;
    }

    public String convertArticleDeltaToJson(List<Map<String,Object>> delta) throws JsonProcessingException {
        if (delta != null) {
            Map<String, List<Map<String, Object>>> wrappedDelta = new HashMap<>();
            wrappedDelta.put("delta", delta); // 包装delta数组与"delta"键
            return objectMapper.writeValueAsString(wrappedDelta); // 转换整个结构为JSON字符串
        }
        return null;
    }

    public List<Map<String, Object>> convertToStandardDeltaFormat(List<Map<String, Object>> nonStandardDelta) {
        List<Map<String, Object>> standardDeltaOps = new ArrayList<>();

        for (Map<String, Object> op : nonStandardDelta) {
            if (op.containsKey("insert") && op.get("insert") instanceof Map) {
                Map<String, Object> insertValue = (Map<String, Object>) op.get("insert");
                if (insertValue.containsKey("image") && insertValue.get("image") instanceof List) {
                    List<String> images = (List<String>) insertValue.get("image");
                    for (String imageUrl : images) {
                        Map<String, Object> imageOp = new HashMap<>();
                        imageOp.put("insert", Collections.singletonMap("image", imageUrl));
                        standardDeltaOps.add(imageOp);
                        // Also add a new line after image as per Quill's standard format
                        standardDeltaOps.add(Collections.singletonMap("insert", "\n"));
                    }
                } else {
                    // If it's not an image or any other type of object, add it as it is
                    standardDeltaOps.add(op);
                }
            } else {
                // If the map doesn't contain 'insert' or is not a Map type, leave it unchanged
                standardDeltaOps.add(op);
            }
        }
        return standardDeltaOps;
    }



}
