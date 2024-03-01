package xyz.dowob.blogspring.functions;

public class PublishRequestDto {
    private String id;
    private boolean isPublished;

    public PublishRequestDto(){}

    public PublishRequestDto(String id, boolean isPublished){
        this.id = id;
        this.isPublished = isPublished;
    }

    public Long getId(){
        return Long.parseLong(id);
    }

    public void setId(String id){
        this.id = id;
    }

    public boolean getIsPublished(){
        return isPublished;
    }

    public void setIsPublished(boolean isPublished){
        this.isPublished = isPublished;
    }
}
