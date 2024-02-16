package xyz.dowob.blogspring.functions;

import java.util.Map;

public class CommentDto {
    private Map<String,Object> delta;
    public CommentDto(){}
    public CommentDto(Map<String,Object> delta){
        this.delta = delta;
    }
    public Map<String, Object> getDelta() {
        return delta;
    }
    public void setDelta(Map<String, Object> delta) {
        this.delta = delta;
    }
}
