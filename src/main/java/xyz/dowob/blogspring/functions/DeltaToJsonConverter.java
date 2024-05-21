package xyz.dowob.blogspring.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.dowob.blogspring.model.Comment;
import xyz.dowob.blogspring.model.Post;

import java.util.*;

public class DeltaToJsonConverter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    public String convertCommentDeltaToJson(Map<String,Object> delta) throws JsonProcessingException {
        if (delta != null) {
            return objectMapper.writeValueAsString(delta);
        }
        return null;
    }

    public String convertArticleDeltaToJson(List<Map<String,Object>> delta) throws JsonProcessingException {
        if (delta != null) {
            Map<String, List<Map<String, Object>>> wrappedDelta = new HashMap<>();
            wrappedDelta.put("delta", delta);
            return objectMapper.writeValueAsString(wrappedDelta);
        }
        return null;
    }

    public Map<String, Object> convertCommentFromJsonToDelta(String json, Comment comment) throws JsonProcessingException {
        if (json != null) {
            Map<String, Object> delta = objectMapper.readValue(json, new TypeReference<>() {});
            if (comment.isDeleted()) {
                Map<String, Object> deletedOpsItem = Collections.singletonMap("insert", "已刪除評論");
                delta.put("ops", Collections.singletonList(deletedOpsItem));
            }
            return delta;
        }
        return null;
    }

    public Map<String, Object> convertArticleFromJsonToDelta(String json, Post post) throws JsonProcessingException {
        if (json != null) {
            Map<String, Object> delta = objectMapper.readValue(json, new TypeReference<>() {});
            if (post.isDeleted()) {
                delta.put("insert", "已刪除的文章");
            }
            return delta;
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
                        standardDeltaOps.add(Collections.singletonMap("insert", "\n"));
                    }
                } else {
                    standardDeltaOps.add(op);
                }
            } else {
                standardDeltaOps.add(op);
            }
        }
        return standardDeltaOps;
    }



}
