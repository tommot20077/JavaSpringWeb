package xyz.dowob.blogspring.functions;

import java.util.Map;

public class CommentDto {
    private Map<String,Object> delta;
    private String imageUrl;

    public CommentDto(){}
    public CommentDto(Map<String,Object> delta){
        this.delta = delta;
    }
    public Map<String, Object> getDelta() {
        return delta;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public void setDelta(Map<String, Object> delta) {
        this.delta = delta;
    }
}
