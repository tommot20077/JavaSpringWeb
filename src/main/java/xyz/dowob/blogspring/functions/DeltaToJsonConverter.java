package xyz.dowob.blogspring.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


}
