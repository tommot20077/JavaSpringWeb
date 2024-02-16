package xyz.dowob.blogspring.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class DeltaToJsonConverter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    public String convertDeltaToJson(Map<String,Object> delta) throws JsonProcessingException {
        if (delta != null) {
            return objectMapper.writeValueAsString(delta);
        }
        return null;
    }
    public Map<String, Object> convertJsonToDelta(String json) throws JsonProcessingException {
        if (json != null) {
            // JSON字符串转换为Map对象
            return objectMapper.readValue(json, new TypeReference<>() {});
        }
        return null;
    }
}
