package xyz.dowob.blogspring.functions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ArticleDto {
    private String title;
    @JsonProperty("delta")
    private List<Map<String,Object>> delta;

    public ArticleDto(){};

    public ArticleDto(String title, List<Map<String,Object>> delta){
        this.title = title;
        this.delta = delta;

    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public List<Map<String,Object>> getDeltaContent() {
        return delta;
    }
    public void setDeltaContent(List<Map<String,Object>> delta) {
        this.delta = delta;
    }

}
