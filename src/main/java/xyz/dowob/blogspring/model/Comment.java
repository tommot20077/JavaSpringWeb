package xyz.dowob.blogspring.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long commentId;
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Post post;
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDeleted;
    private int commentInArticleId;

    @Column(name = "update_time", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }




    public long getCommentId() {
        return commentId;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }
    public Post getPost() {
        return post;
    }
    public void setPost(Post post) {
        this.post = post;
    }
    public boolean isDeleted() {
        return isDeleted;
    }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    public int getCommentInArticleId() {
        return commentInArticleId;
    }
    public void setCommentInArticleId(int commentInArticleId) {
        this.commentInArticleId = commentInArticleId;
    }

    public Date getUpdate_time() {
        return updateTime;
    }

}
