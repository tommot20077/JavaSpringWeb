package xyz.dowob.blogspring.functions;

public class ImageDto {
    private final String imageUrl;
    private final String articleId;

    public ImageDto(String imageUrl, String articleId) {
        this.imageUrl = imageUrl;
        this.articleId = articleId;
    }

    public String getUrl() {
        return imageUrl;
    }

    public Long getArticleId() {
        return Long.parseLong(articleId);
    }
}
